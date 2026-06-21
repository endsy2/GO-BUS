package com.busapp.busservice.service;

import com.busapp.busservice.dto.event.SeatAvailabilityEvent;

public interface WebSocketService {
    void broadcastSeatAvailability(SeatAvailabilityEvent event);
}
