package com.busapp.busservice.service.impl;

import com.busapp.busservice.dto.event.SeatAvailabilityEvent;
import com.busapp.busservice.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastSeatAvailability(SeatAvailabilityEvent event) {
        String destination = "/topic/schedule/" + event.getScheduleId() + "/seats";
        
        log.info("[WEBSOCKET] Broadcasting seat availability - type={}, scheduleId={}, seatId={}, seatNumber={}, status={}, destination={}", 
                event.getType(), event.getScheduleId(), event.getSeatId(), event.getSeatNumber(), 
                event.getStatus(), destination);
        
        try {
            messagingTemplate.convertAndSend(destination, event);
            
            log.debug("[WEBSOCKET] Seat availability broadcasted successfully - type={}, seatId={}, bookingId={}", 
                    event.getType(), event.getSeatId(), event.getBookingId());
            
        } catch (Exception e) {
            log.error("[WEBSOCKET] Failed to broadcast seat availability - type={}, scheduleId={}, seatId={}, destination={}, error={}", 
                    event.getType(), event.getScheduleId(), event.getSeatId(), destination, e.getMessage(), e);
        }
    }
}
