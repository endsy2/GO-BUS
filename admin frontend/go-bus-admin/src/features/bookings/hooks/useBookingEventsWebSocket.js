import { useState, useEffect, useRef } from 'react';
import { bookingWebsocketService } from 'shared/services/websocketService';

/**
 * Subscribe to real-time booking lifecycle events for the admin bookings list.
 *
 * Events are broadcast by booking-service on the `/topic/admin/bookings` topic
 * whenever a booking is created, paid, or cancelled. Each message has the shape:
 *
 *   { action: 'CREATED' | 'PAYMENT_UPDATED' | 'CANCELLED' | 'UPDATED',
 *     bookingId: number,
 *     booking: BookingResponse,   // full row, ready to merge into the table
 *     timestamp: string }
 *
 * Design mirrors useMultiScheduleWebSocket:
 *  - isConnected is reactive state driven by the service's event emitter.
 *  - onBookingEvent is stored in a ref so its identity can change without
 *    re-subscribing.
 *  - A single useEffect handles subscribe + cleanup.
 *
 * NOTE: this uses the booking-service connection (bookingWebsocketService), which
 * is separate from the bus-service connection used for seat updates — the two
 * brokers are independent.
 *
 * @param {Function} onBookingEvent - Callback for each incoming booking event.
 * @param {boolean}  enabled        - Set to false to suspend the subscription.
 */
export const useBookingEventsWebSocket = (onBookingEvent, enabled = true) => {
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

  const callbackRef = useRef(onBookingEvent);
  useEffect(() => {
    callbackRef.current = onBookingEvent;
  });

  useEffect(() => {
    if (!enabled) return undefined;

    const topic = '/topic/admin/bookings';
    const handler = (data) => {
      if (!data?.action) {
        console.warn('[useBookingEventsWebSocket] Unexpected message format:', data);
        return;
      }
      callbackRef.current?.(data);
    };

    bookingWebsocketService.subscribe(topic, handler);
    return () => bookingWebsocketService.unsubscribe(topic);
  }, [enabled]);

  return { isConnected };
};

export default useBookingEventsWebSocket;
