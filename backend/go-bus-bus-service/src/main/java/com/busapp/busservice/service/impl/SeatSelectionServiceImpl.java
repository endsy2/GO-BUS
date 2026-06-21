package com.busapp.busservice.service.impl;

import com.busapp.busservice.dto.event.SeatAvailabilityEvent;
import com.busapp.busservice.exception.BadRequestException;
import com.busapp.busservice.exception.ResourceNotFoundException;
import com.busapp.busservice.model.ScheduleSeat;
import com.busapp.busservice.model.enums.SeatStatus;
import com.busapp.busservice.model.repository.ScheduleSeatRepository;
import com.busapp.busservice.service.SeatSelectionService;
import com.busapp.busservice.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatSelectionServiceImpl implements SeatSelectionService {

    private final ScheduleSeatRepository scheduleSeatRepository;
    private final WebSocketService webSocketService;
    
    /**
     * Seat selection timeout in minutes (default: 5 minutes)
     * Can be configured via application.yml: seat.selection.timeout-minutes
     */
    @Value("${seat.selection.timeout-minutes:5}")
    private int selectionTimeoutMinutes;

    @Override
    @Transactional
    public void selectSeat(Long scheduleId, Long seatId, Long userId) {
        ScheduleSeat scheduleSeat = scheduleSeatRepository
                .findByScheduleIdAndSeatId(scheduleId, seatId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seat " + seatId + " not found for schedule " + scheduleId));
        
        // Only allow selection if seat is AVAILABLE
        if (scheduleSeat.getStatus() != SeatStatus.AVAILABLE) {
            throw new BadRequestException(
                    "Seat " + scheduleSeat.getSeat().getSeatNumber() + 
                    " is not available (current status: " + scheduleSeat.getStatus() + ")");
        }
        
        // Mark seat as PENDING
        scheduleSeat.setStatus(SeatStatus.PENDING);
        scheduleSeat.setPendingUserId(userId);
        scheduleSeat.setPendingAt(LocalDateTime.now());
        
        scheduleSeatRepository.save(scheduleSeat);
        
        log.info("Seat selected: scheduleId={}, seatId={}, userId={}", 
                scheduleId, seatId, userId);
        
        // Broadcast to all clients
        broadcastSeatEvent("SEAT_SELECTED", scheduleSeat, userId);
    }

    @Override
    @Transactional
    public void deselectSeat(Long scheduleId, Long seatId, Long userId) {
        ScheduleSeat scheduleSeat = scheduleSeatRepository
                .findByScheduleIdAndSeatId(scheduleId, seatId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seat " + seatId + " not found for schedule " + scheduleId));
        
        // Only allow deselection if seat is PENDING and belongs to this user
        if (scheduleSeat.getStatus() != SeatStatus.PENDING) {
            log.warn("Attempted to deselect seat that is not pending: scheduleId={}, seatId={}, status={}", 
                    scheduleId, seatId, scheduleSeat.getStatus());
            return;
        }
        
        if (!userId.equals(scheduleSeat.getPendingUserId())) {
            throw new BadRequestException(
                    "Cannot deselect seat - it was selected by another user");
        }
        
        // Mark seat as AVAILABLE
        scheduleSeat.setStatus(SeatStatus.AVAILABLE);
        scheduleSeat.setPendingUserId(null);
        scheduleSeat.setPendingAt(null);
        
        scheduleSeatRepository.save(scheduleSeat);
        
        log.info("Seat deselected: scheduleId={}, seatId={}, userId={}", 
                scheduleId, seatId, userId);
        
        // Broadcast to all clients
        broadcastSeatEvent("SEAT_DESELECTED", scheduleSeat, null);
    }

    /**
     * Scheduled task to release expired pending seats
     * Runs every minute
     */
    @Override
    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    @Transactional
    public void releaseExpiredPendingSeats() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(selectionTimeoutMinutes);
        
        List<ScheduleSeat> expiredSeats = scheduleSeatRepository
                .findByStatusAndPendingAtBefore(SeatStatus.PENDING, expiryTime);
        
        if (expiredSeats.isEmpty()) {
            return;
        }
        
        log.info("Releasing {} expired pending seats (timeout: {} minutes)", 
                expiredSeats.size(), selectionTimeoutMinutes);
        
        expiredSeats.forEach(scheduleSeat -> {
            scheduleSeat.setStatus(SeatStatus.AVAILABLE);
            scheduleSeat.setPendingUserId(null);
            scheduleSeat.setPendingAt(null);
            
            // Broadcast expiry to all clients
            broadcastSeatEvent("SEAT_SELECTION_EXPIRED", scheduleSeat, null);
        });
        
        scheduleSeatRepository.saveAll(expiredSeats);
        
        log.info("Released {} expired pending seats", expiredSeats.size());
    }
    
    /**
     * Helper method to broadcast seat events via WebSocket
     */
    private void broadcastSeatEvent(String type, ScheduleSeat scheduleSeat, Long userId) {
        try {
            SeatAvailabilityEvent event = SeatAvailabilityEvent.builder()
                    .type(type)
                    .scheduleId(scheduleSeat.getSchedule().getId())
                    .seatId(scheduleSeat.getSeat().getId())
                    .seatNumber(scheduleSeat.getSeat().getSeatNumber())
                    .bookingId(scheduleSeat.getBookingId())
                    .status(scheduleSeat.getStatus().name())
                    .userId(userId) // User who selected/deselected the seat
                    .timestamp(LocalDateTime.now())
                    .build();
            
            webSocketService.broadcastSeatAvailability(event);
        } catch (Exception e) {
            log.error("Failed to broadcast seat event: {}", e.getMessage(), e);
        }
    }
}
