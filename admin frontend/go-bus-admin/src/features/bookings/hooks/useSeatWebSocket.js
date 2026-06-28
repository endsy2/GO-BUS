import { useState, useEffect, useRef, useCallback } from 'react';
import websocketService from 'shared/services/websocketService';

/**
 * Subscribe to real-time seat availability updates for a single schedule.
 *
 * Design decisions
 * ────────────────
 * 1. The onSeatUpdate callback is kept in a ref so its identity can change
 *    every render without restarting the subscription. This breaks the
 *    old callback → useCallback → useEffect dependency chain that was
 *    causing re-subscriptions on every parent render.
 *
 * 2. isConnected is actual React state (not a snapshot value), updated via
 *    the service's event emitter. This makes the status indicator reactive
 *    without polling or extra useEffects.
 *
 * 3. There is a single useEffect keyed only on [scheduleId, enabled].
 *    It creates one subscription and tears it down cleanly on unmount or
 *    when those values change. No second "disconnect" effect.
 *
 * @param {number|null} scheduleId   - The schedule to subscribe to (null = inactive)
 * @param {Function}    onSeatUpdate - Callback invoked with each incoming seat event
 * @param {boolean}     enabled      - Set to false to suspend the subscription
 */
export const useSeatWebSocket = (scheduleId, onSeatUpdate, enabled = true) => {
  // ── Reactive connection state ──────────────────────────────────────────────
  const [isConnected, setIsConnected] = useState(() => websocketService.isConnected());

  useEffect(() => {
    const unsubs = [
      websocketService.on('connect', () => setIsConnected(true)),
      websocketService.on('disconnect', () => setIsConnected(false)),
    ];
    return () => unsubs.forEach((fn) => fn());
  }, []);

  // ── Stable callback ref ────────────────────────────────────────────────────
  // Updating this ref never triggers a re-subscription — the Effect below
  // does not depend on the callback at all.
  const callbackRef = useRef(onSeatUpdate);
  useEffect(() => {
    callbackRef.current = onSeatUpdate;
  });

  // ── Subscription lifecycle ─────────────────────────────────────────────────
  useEffect(() => {
    if (!enabled || !scheduleId) return; // Nothing to subscribe to

    const topic = `/topic/schedule/${scheduleId}/seats`;

    const handler = (data) => {
      if (!data?.type) {
        console.warn('[useSeatWebSocket] Unexpected message format:', data);
        return;
      }
      callbackRef.current?.(data);
    };

    websocketService.subscribe(topic, handler);

    // Cleanup: unsubscribe when scheduleId changes, enabled becomes false,
    // or the component unmounts. The service's singleton connection stays alive.
    return () => {
      websocketService.unsubscribe(topic);
    };
    // callbackRef is intentionally excluded from deps — it never needs to
    // trigger a re-subscription (that's the whole point of using a ref).
  }, [scheduleId, enabled]);

  // ── Outbound message sender ────────────────────────────────────────────────
  const sendMessage = useCallback((destination, body) => {
    if (!websocketService.isConnected()) {
      console.error('[useSeatWebSocket] Cannot send — WebSocket not connected');
      return false;
    }
    try {
      websocketService.send(destination, body);
      return true;
    } catch (err) {
      console.error('[useSeatWebSocket] Send failed:', err);
      return false;
    }
  }, []);

  return { isConnected, sendMessage };
};

export default useSeatWebSocket;
