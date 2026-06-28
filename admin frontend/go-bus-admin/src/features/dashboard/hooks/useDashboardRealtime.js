import { useState, useEffect, useRef } from 'react';
import { bookingWebsocketService } from 'shared/services/websocketService';

/**
 * Subscribe to real-time dashboard updates (topic /topic/admin/dashboard).
 *
 * booking-service broadcasts a DashboardUpdateEvent whenever something that
 * affects the dashboard happens (new booking, payment success, refund, etc.).
 * Rather than trust the partial counters carried by each event, we treat every
 * event as a trigger to silently re-pull the dashboard data — guaranteeing the
 * cards AND charts stay accurate.
 *
 * Bursts of events (e.g. a booking that fires NEW_BOOKING + payment success in
 * quick succession) are coalesced into a single refresh via a trailing debounce.
 *
 * Uses bookingWebsocketService (the booking-service connection) because the
 * dashboard topic lives on that broker, not the bus-service one used for seats.
 *
 * @param {Function} onUpdate   - Called (debounced) when a dashboard event arrives.
 * @param {boolean}  enabled    - Set to false to suspend the subscription.
 * @param {number}   debounceMs - Trailing debounce window, default 800ms.
 */
export const useDashboardRealtime = (onUpdate, enabled = true, debounceMs = 800) => {
  const [isConnected, setIsConnected] = useState(() =>
    bookingWebsocketService.isConnected()
  );

  useEffect(() => {
    const unsubs = [
      bookingWebsocketService.on('connect', () => setIsConnected(true)),
      bookingWebsocketService.on('disconnect', () => setIsConnected(false)),
    ];
    return () => unsubs.forEach((fn) => fn());
  }, []);

  // Keep the latest callback without re-subscribing.
  const callbackRef = useRef(onUpdate);
  useEffect(() => {
    callbackRef.current = onUpdate;
  });

  useEffect(() => {
    if (!enabled) return undefined;

    const topic = '/topic/admin/dashboard';
    let timer = null;

    const handler = (event) => {
      // Trailing debounce: collapse a burst of events into one refresh.
      if (timer) clearTimeout(timer);
      timer = setTimeout(() => {
        timer = null;
        callbackRef.current?.(event);
      }, debounceMs);
    };

    bookingWebsocketService.subscribe(topic, handler);

    return () => {
      if (timer) clearTimeout(timer);
      bookingWebsocketService.unsubscribe(topic);
    };
  }, [enabled, debounceMs]);

  return { isConnected };
};

export default useDashboardRealtime;
