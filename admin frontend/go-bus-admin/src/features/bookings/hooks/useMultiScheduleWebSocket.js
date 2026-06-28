import { useState, useEffect, useRef } from 'react';
import websocketService from 'shared/services/websocketService';

/**
 * Subscribe to real-time seat availability updates for multiple schedules.
 *
 * Design decisions
 * ────────────────
 * 1. scheduleIds is serialised into a sorted key string before being used as
 *    a useEffect dependency. This prevents React from seeing a "new" array
 *    reference on every render (which was causing constant re-subscriptions
 *    in BookingsPage because the array was created inline).
 *
 * 2. The onSeatUpdate callback is stored in a ref so its identity can change
 *    without restarting any subscription.
 *
 * 3. isConnected is actual React state driven by the service's event emitter,
 *    not a snapshot value — so the UI stays accurate without polling.
 *
 * 4. A single useEffect handles subscribe AND cleanup. When the set of IDs
 *    changes, React automatically runs the previous cleanup (unsubscribing old
 *    topics) before running the new effect (subscribing to the new set).
 *
 * @param {number[]} scheduleIds   - Schedule IDs to monitor (safe to pass inline)
 * @param {Function} onSeatUpdate  - Callback for each incoming seat event
 * @param {boolean}  enabled       - Set to false to suspend all subscriptions
 */
export const useMultiScheduleWebSocket = (
  scheduleIds = [],
  onSeatUpdate,
  enabled = true
) => {
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
  const callbackRef = useRef(onSeatUpdate);
  useEffect(() => {
    callbackRef.current = onSeatUpdate;
  });

  // ── Stable key: re-subscribe only when the *set* of IDs actually changes ──
  // Sort so that [3,1,2] and [1,2,3] produce the same key and don't retrigger.
  const scheduleIdsKey = [...scheduleIds]
    .filter(Boolean)
    .sort((a, b) => a - b)
    .join(',');

  // ── Subscription lifecycle ─────────────────────────────────────────────────
  useEffect(() => {
    if (!enabled || !scheduleIdsKey) return; // Nothing to subscribe to

    const ids = scheduleIdsKey.split(',').map(Number);

    // Single shared handler for all topics subscribed in this effect
    const handler = (data) => {
      if (!data?.type) {
        console.warn('[useMultiScheduleWebSocket] Unexpected message format:', data);
        return;
      }
      callbackRef.current?.(data);
    };

    ids.forEach((id) => {
      websocketService.subscribe(`/topic/schedule/${id}/seats`, handler);
    });

    // Cleanup: unsubscribe exactly the topics we subscribed to in this effect.
    // React runs this before re-executing the effect when deps change, so
    // removed IDs are always cleaned up before new ones are added.
    return () => {
      ids.forEach((id) => {
        websocketService.unsubscribe(`/topic/schedule/${id}/seats`);
      });
    };
  }, [scheduleIdsKey, enabled]);

  // ── Derived values for callers ─────────────────────────────────────────────
  const subscribedSchedules = scheduleIds.filter((id) =>
    websocketService.getSubscribedTopics().includes(`/topic/schedule/${id}/seats`)
  );

  return { isConnected, subscribedSchedules };
};

export default useMultiScheduleWebSocket;
