package com.busapp.busservice.controller;

import com.busapp.busservice.dto.request.SeatSelectionRequest;
import com.busapp.busservice.service.SeatSelectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for handling real-time seat selection/deselection.
 * Clients send messages to /app/seat/select or /app/seat/deselect
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class SeatSelectionController {

    private final SeatSelectionService seatSelectionService;

    /**
     * Handle seat selection from frontend.
     * Client sends: { scheduleId: 123, seatId: 456, userId: 789 }
     * Broadcasts to: /topic/schedule/{scheduleId}/seats
     */
    @MessageMapping("/seat/select")
    public void selectSeat(@Payload SeatSelectionRequest request, 
                          SimpMessageHeaderAccessor headerAccessor) {
        try {
            log.info("Seat selection request: scheduleId={}, seatId={}, userId={}", 
                    request.getScheduleId(), request.getSeatId(), request.getUserId());
            
            seatSelectionService.selectSeat(
                    request.getScheduleId(), 
                    request.getSeatId(), 
                    request.getUserId()
            );
        } catch (Exception e) {
            log.error("Failed to process seat selection: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle seat deselection from frontend.
     * Client sends: { scheduleId: 123, seatId: 456, userId: 789 }
     * Broadcasts to: /topic/schedule/{scheduleId}/seats
     */
    @MessageMapping("/seat/deselect")
    public void deselectSeat(@Payload SeatSelectionRequest request,
                            SimpMessageHeaderAccessor headerAccessor) {
        try {
            log.info("Seat deselection request: scheduleId={}, seatId={}, userId={}", 
                    request.getScheduleId(), request.getSeatId(), request.getUserId());
            
            seatSelectionService.deselectSeat(
                    request.getScheduleId(), 
                    request.getSeatId(), 
                    request.getUserId()
            );
        } catch (Exception e) {
            log.error("Failed to process seat deselection: {}", e.getMessage(), e);
        }
    }
}
