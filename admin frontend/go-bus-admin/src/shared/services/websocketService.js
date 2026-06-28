import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL =
  process.env.REACT_APP_WS_URL ||
  'https://go-bus-gateway-service-production.up.railway.app/bus-service/ws/bus';

// Booking-service WebSocket endpoint (gateway route /booking-service/ws/booking/**).
// Booking lifecycle events (/topic/admin/bookings, /topic/admin/dashboard) are
// published on the booking-service broker, which is a SEPARATE in-memory broker
// from bus-service — so it needs its own connection, not the bus-service one.
const BOOKING_WS_URL =
  process.env.REACT_APP_WS_BOOKING_URL ||
  (process.env.REACT_APP_BASE_URL
    ? `${process.env.REACT_APP_BASE_URL}/booking-service/ws/booking`
    : 'https://go-bus-gateway-service-production.up.railway.app/booking-service/ws/booking');

// Conservative reconnect strategy: 5 s fixed delay, give up after 8 failed attempts.
// Do NOT set this lower than 3000 — rapid reconnects destabilise the server.
const RECONNECT_DELAY_MS = 5000;
const MAX_RECONNECT_ATTEMPTS = 8;

class WebSocketService {
  /**
   * @param {string} url - SockJS endpoint this instance connects to. Defaults to
   *   the bus-service endpoint so existing callers (seat features) are unaffected.
   */
  constructor(url = WS_URL) {
    this.url = url;
    this.client = null;

    /** topic (string) → active STOMP Subscription object */
    this.subscriptions = new Map();

    /** topic (string) → callback, queued while the connection is being established */
    this.pendingSubscriptions = new Map();

    /** Guards against concurrent connect() calls returning different Promises */
    this.isConnecting = false;
    this._connectPromise = null;

    /** Incremented on each WebSocket close; reset on successful connect */
    this.reconnectAttempts = 0;

    /**
     * Set to true before an intentional disconnect so that onWebSocketClose
     * does not count the closure as a failed reconnect attempt.
     */
    this._intentionalDisconnect = false;

    /** Lightweight event bus: 'connect' | 'disconnect' | 'error' → Set<fn> */
    this._eventListeners = new Map();
  }

  // ─── Auth ──────────────────────────────────────────────────────────────────

  _getToken() {
    try {
      const data = JSON.parse(localStorage.getItem('user') || 'null');
      return data?.token || data?.accessToken || null;
    } catch {
      return null;
    }
  }

  // ─── Lightweight event emitter ────────────────────────────────────────────

  /**
   * Register a listener for 'connect', 'disconnect', or 'error'.
   * Returns an unsubscribe function — call it in useEffect cleanup.
   */
  on(event, fn) {
    if (!this._eventListeners.has(event)) {
      this._eventListeners.set(event, new Set());
    }
    this._eventListeners.get(event).add(fn);
    return () => this._eventListeners.get(event)?.delete(fn);
  }

  _emit(event, payload) {
    this._eventListeners.get(event)?.forEach((fn) => {
      try {
        fn(payload);
      } catch (_) {
        // Swallow errors from listeners so they cannot break the service
      }
    });
  }

  // ─── Connection ────────────────────────────────────────────────────────────

  /**
   * Connect to the WebSocket server.
   *
   * • Idempotent — returns immediately if already connected.
   * • Deduplicates concurrent calls — all callers share the same Promise.
   * • Reconnection is handled EXCLUSIVELY by the STOMP client's built-in
   *   reconnectDelay. There is no manual reconnect anywhere in this class.
   */
  connect() {
    // Already connected — nothing to do
    if (this.client?.connected) return Promise.resolve();

    // Concurrent call — share the in-flight Promise instead of opening a
    // second connection
    if (this.isConnecting && this._connectPromise) {
      return this._connectPromise;
    }

    const token = this._getToken();
    if (!token) {
      return Promise.reject(
        new Error('[WebSocket] No authentication token. Please log in again.')
      );
    }

    this.isConnecting = true;
    this._intentionalDisconnect = false;

    // Deactivate any stale client from a previous session
    if (this.client) {
      try {
        this.client.deactivate();
      } catch (_) {}
      this.client = null;
    }

    this._connectPromise = new Promise((resolve, reject) => {
      let settled = false;
      const settle = (fn, value) => {
        if (settled) return;
        settled = true;
        fn(value);
      };

      this.client = new Client({
        /**
         * webSocketFactory is called on the initial connect AND on every
         * automatic reconnect. Reading the token here (instead of capturing
         * it once at construction time) means reconnects use a fresh token —
         * essential if the access token was silently refreshed between attempts.
         */
        webSocketFactory: () => {
          const freshToken = this._getToken();
          const url = freshToken
            ? `${this.url}?token=${encodeURIComponent(freshToken)}`
            : this.url;
          return new SockJS(url);
        },

        /**
         * beforeConnect fires just before the STOMP CONNECT frame is sent,
         * giving us a chance to update the connect headers with a fresh token
         * for every connection attempt (initial + reconnects).
         */
        beforeConnect: () => {
          const freshToken = this._getToken();
          if (freshToken && this.client) {
            this.client.connectHeaders = { Authorization: `Bearer ${freshToken}` };
          }
        },

        debug: (str) => {
          // Only surface high-signal STOMP frames in the console
          if (
            str.includes('ERROR') ||
            str.includes('CONNECT') ||
            str.includes('DISCONNECT')
          ) {
            console.debug('[STOMP]', str.slice(0, 200));
          }
        },

        /**
         * Built-in reconnect — the ONLY reconnect mechanism in this service.
         * There is intentionally no manual handleReconnect / setTimeout anywhere.
         * A previous version had both, causing double (and escalating) reconnects.
         */
        reconnectDelay: RECONNECT_DELAY_MS,

        // Heartbeats keep NAT/proxy connections alive without hammering the server
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,

        onConnect: () => {
          console.log('[WebSocket] ✅ Connected');
          this.isConnecting = false;
          this.reconnectAttempts = 0;
          this._emit('connect');
          this._flushPendingSubscriptions();
          settle(resolve, undefined);
        },

        onStompError: (frame) => {
          const msg = frame.headers?.message || 'STOMP error';
          console.error('[WebSocket] STOMP error:', msg);
          this.isConnecting = false;
          this._connectPromise = null;
          this._emit('error', msg);
          settle(reject, new Error(msg));
        },

        onWebSocketError: (error) => {
          console.error('[WebSocket] WebSocket error:', error);
          this.isConnecting = false;
          this._connectPromise = null;
          settle(reject, error);
        },

        onWebSocketClose: (event) => {
          if (this._intentionalDisconnect) return;

          this.reconnectAttempts++;
          this._emit('disconnect');
          console.log(
            `[WebSocket] Closed (attempt ${this.reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS}), code=${event.code}`
          );

          if (this.reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            console.error(
              '[WebSocket] Max reconnect attempts reached — giving up'
            );
            // Set the flag BEFORE deactivating so the resulting close event is
            // not treated as another failed reconnect attempt
            this._intentionalDisconnect = true;
            try {
              this.client?.deactivate();
            } catch (_) {}
            this._emit('error', 'max_reconnect_reached');
          }
        },

        /**
         * onDisconnect fires on a clean session end (e.g. server DISCONNECT frame)
         * OR just before a reconnect cycle begins.
         *
         * DO NOT trigger manual reconnection here. The built-in reconnectDelay
         * already schedules the next attempt. Adding a manual reconnect here was
         * the primary cause of rapid-fire double reconnects in the old version.
         */
        onDisconnect: () => {
          this.isConnecting = false;
          console.log('[WebSocket] Session disconnected');
        },
      });

      console.log('[WebSocket] Activating client…');
      this.client.activate();
    });

    return this._connectPromise;
  }

  // ─── Subscriptions ─────────────────────────────────────────────────────────

  /**
   * Flush all subscriptions that were queued while a connection was in progress.
   * Called immediately after onConnect.
   */
  _flushPendingSubscriptions() {
    if (this.pendingSubscriptions.size === 0) return;
    const pending = new Map(this.pendingSubscriptions);
    this.pendingSubscriptions.clear();
    pending.forEach((callback, topic) => this._doSubscribe(topic, callback));
    console.log(`[WebSocket] Flushed ${pending.size} pending subscription(s)`);
  }

  /**
   * Low-level subscribe. Always call subscribe() from outside — this is
   * the inner implementation used after the connection is confirmed.
   */
  _doSubscribe(topic, callback) {
    if (!this.client?.connected) {
      // Still not connected — queue for later (e.g. reconnect flush)
      this.pendingSubscriptions.set(topic, callback);
      return null;
    }

    // Prevent duplicate subscriptions: if a subscription already exists for
    // this topic, unsubscribe it first before creating a new one.
    const existing = this.subscriptions.get(topic);
    if (existing) {
      try {
        existing.unsubscribe();
      } catch (_) {}
      this.subscriptions.delete(topic);
    }

    try {
      const sub = this.client.subscribe(topic, (msg) => {
        try {
          callback(JSON.parse(msg.body));
        } catch (err) {
          console.error('[WebSocket] Failed to parse message:', err);
        }
      });
      this.subscriptions.set(topic, sub);
      console.log(`[WebSocket] Subscribed: ${topic}`);
      return sub;
    } catch (err) {
      console.error(`[WebSocket] Subscribe failed (${topic}):`, err);
      return null;
    }
  }

  /**
   * Subscribe to a topic. Safe to call before the connection is established —
   * the subscription will be queued and applied as soon as onConnect fires.
   */
  subscribe(topic, callback) {
    if (!this.client?.connected) {
      // Queue the subscription
      this.pendingSubscriptions.set(topic, callback);
      // Trigger a connection if one is not already in progress
      if (!this.isConnecting) {
        this.connect().catch((err) =>
          console.error('[WebSocket] Connect for subscription failed:', err)
        );
      }
      return null;
    }
    return this._doSubscribe(topic, callback);
  }

  /** Remove a subscription by topic. Also removes it from the pending queue. */
  unsubscribe(topic) {
    this.pendingSubscriptions.delete(topic);
    const sub = this.subscriptions.get(topic);
    if (sub) {
      try {
        sub.unsubscribe();
      } catch (_) {}
      this.subscriptions.delete(topic);
      console.log(`[WebSocket] Unsubscribed: ${topic}`);
    }
  }

  /** Remove all active and pending subscriptions. */
  unsubscribeAll() {
    this.pendingSubscriptions.clear();
    this.subscriptions.forEach((sub) => {
      try {
        sub.unsubscribe();
      } catch (_) {}
    });
    this.subscriptions.clear();
    console.log('[WebSocket] All subscriptions cleared');
  }

  // ─── Messaging ─────────────────────────────────────────────────────────────

  send(destination, body) {
    if (!this.client?.connected) {
      throw new Error('[WebSocket] Cannot send — not connected');
    }
    this.client.publish({ destination, body: JSON.stringify(body) });
  }

  // ─── Lifecycle ─────────────────────────────────────────────────────────────

  /**
   * Fully disconnect and clean up. Call this on logout, not on component
   * unmount — the service is a long-lived singleton shared across features.
   */
  disconnect() {
    this._intentionalDisconnect = true;
    this.unsubscribeAll();
    if (this.client) {
      try {
        this.client.deactivate();
      } catch (_) {}
      this.client = null;
    }
    this.isConnecting = false;
    this._connectPromise = null;
    this.reconnectAttempts = 0;
    console.log('[WebSocket] Disconnected');
    this._emit('disconnect');
  }

  isConnected() {
    return this.client?.connected === true;
  }

  /** Returns the list of topics that currently have active subscriptions. */
  getSubscribedTopics() {
    return [...this.subscriptions.keys()];
  }
}

// Singleton — one connection for the entire app lifetime (bus-service: seat updates)
const websocketService = new WebSocketService();
export default websocketService;

// Separate singleton for the booking-service broker (booking lifecycle + dashboard
// events). Kept distinct because STOMP simple brokers are per-service and in-memory.
export const bookingWebsocketService = new WebSocketService(BOOKING_WS_URL);
