import React, { useState } from 'react';
import { useMultiScheduleWebSocket } from '../../hooks/useMultiScheduleWebSocket';
import { Button } from 'shared/components/common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'shared/components/ui/card';
import { Wifi, WifiOff, Activity } from 'lucide-react';

/**
 * WebSocket Tester Component
 * Use this component to test WebSocket connections and monitor events
 * Add it to any page temporarily for debugging
 */
const WebSocketTester = () => {
  const [scheduleIds, setScheduleIds] = useState([1]);
  const [events, setEvents] = useState([]);
  const [enabled, setEnabled] = useState(true);
  const [newScheduleId, setNewScheduleId] = useState('');

  const handleSeatUpdate = (data) => {
    const timestamp = new Date().toLocaleTimeString();
    setEvents(prev => [{
      ...data,
      receivedAt: timestamp
    }, ...prev].slice(0, 20)); // Keep last 20 events
  };

  const { isConnected, subscribedSchedules } = useMultiScheduleWebSocket(
    scheduleIds,
    handleSeatUpdate,
    enabled
  );

  const addSchedule = () => {
    const id = parseInt(newScheduleId);
    if (id && !scheduleIds.includes(id)) {
      setScheduleIds([...scheduleIds, id]);
      setNewScheduleId('');
    }
  };

  const removeSchedule = (id) => {
    setScheduleIds(scheduleIds.filter(sid => sid !== id));
  };

  const clearEvents = () => {
    setEvents([]);
  };

  return (
    <Card className="bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Activity className="w-5 h-5" />
          WebSocket Tester
          {isConnected ? (
            <span className="flex items-center gap-1 text-sm font-normal text-green-500">
              <Wifi className="w-4 h-4" />
              Connected
            </span>
          ) : (
            <span className="flex items-center gap-1 text-sm font-normal text-slate-400">
              <WifiOff className="w-4 h-4" />
              Disconnected
            </span>
          )}
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Connection Control */}
        <div className="flex items-center gap-2">
          <Button
            variant={enabled ? 'danger' : 'success'}
            onClick={() => setEnabled(!enabled)}
            className="flex-1"
          >
            {enabled ? 'Disable WebSocket' : 'Enable WebSocket'}
          </Button>
          <Button
            variant="secondary"
            onClick={clearEvents}
          >
            Clear Events
          </Button>
        </div>

        {/* Schedule Management */}
        <div className="space-y-2">
          <h3 className="text-sm font-semibold text-slate-900 dark:text-white">
            Monitored Schedules
          </h3>
          <div className="flex gap-2">
            <input
              type="number"
              value={newScheduleId}
              onChange={(e) => setNewScheduleId(e.target.value)}
              placeholder="Schedule ID"
              className="flex-1 px-3 py-2 border border-slate-300 dark:border-slate-700 rounded-lg bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
              onKeyPress={(e) => e.key === 'Enter' && addSchedule()}
            />
            <Button onClick={addSchedule} variant="primary">
              Add
            </Button>
          </div>
          <div className="flex flex-wrap gap-2">
            {scheduleIds.map(id => (
              <div
                key={id}
                className="flex items-center gap-2 px-3 py-1 bg-blue-100 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 rounded-full text-sm"
              >
                Schedule #{id}
                {subscribedSchedules.includes(id) && (
                  <span className="text-green-500">✓</span>
                )}
                <button
                  onClick={() => removeSchedule(id)}
                  className="ml-1 text-red-500 hover:text-red-700"
                >
                  ×
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Events Log */}
        <div className="space-y-2">
          <h3 className="text-sm font-semibold text-slate-900 dark:text-white">
            Events Log ({events.length})
          </h3>
          <div className="max-h-96 overflow-y-auto space-y-2 bg-slate-50 dark:bg-slate-800/50 rounded-lg p-3">
            {events.length === 0 ? (
              <p className="text-sm text-slate-500 dark:text-slate-400 text-center py-4">
                No events received yet. Waiting for seat updates...
              </p>
            ) : (
              events.map((event, index) => (
                <div
                  key={index}
                  className={`p-3 rounded-lg border ${
                    event.type === 'SEAT_BOOKED'
                      ? 'bg-green-50 dark:bg-green-900/10 border-green-200 dark:border-green-800'
                      : 'bg-blue-50 dark:bg-blue-900/10 border-blue-200 dark:border-blue-800'
                  }`}
                >
                  <div className="flex justify-between items-start mb-2">
                    <span className={`text-xs font-semibold ${
                      event.type === 'SEAT_BOOKED'
                        ? 'text-green-700 dark:text-green-300'
                        : 'text-blue-700 dark:text-blue-300'
                    }`}>
                      {event.type}
                    </span>
                    <span className="text-xs text-slate-500 dark:text-slate-400">
                      {event.receivedAt}
                    </span>
                  </div>
                  <div className="text-sm space-y-1">
                    <div className="text-slate-900 dark:text-white">
                      <strong>Schedule:</strong> #{event.scheduleId} | 
                      <strong> Seat:</strong> {event.seatNumber} (ID: {event.seatId})
                    </div>
                    {event.bookingId && (
                      <div className="text-slate-700 dark:text-slate-300">
                        <strong>Booking ID:</strong> {event.bookingId}
                      </div>
                    )}
                    <div className="text-slate-600 dark:text-slate-400">
                      <strong>Status:</strong> {event.status}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Connection Info */}
        <div className="text-xs text-slate-500 dark:text-slate-400 space-y-1">
          <div>WebSocket URL: {process.env.REACT_APP_WS_URL || 'ws://localhost:8080/bus-service/ws/bus'}</div>
          <div>Subscribed Topics: {subscribedSchedules.length}</div>
          <div>
            Topics: {subscribedSchedules.map(id => `/topic/schedule/${id}/seats`).join(', ') || 'None'}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default WebSocketTester;
