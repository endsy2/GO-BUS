package com.busapp.busservice.service.impl;

import com.busapp.busservice.dto.ScheduleSeatDetailResponse;
import com.busapp.busservice.dto.ScheduleSeatResponse;
import com.busapp.busservice.exception.BadRequestException;
import com.busapp.busservice.exception.ResourceNotFoundException;
import com.busapp.busservice.mapper.ScheduleSeatMapper;
import com.busapp.busservice.model.BusSchedule;
import com.busapp.busservice.model.ScheduleSeat;
import com.busapp.busservice.model.Seat;
import com.busapp.busservice.model.enums.SeatStatus;
import com.busapp.busservice.model.repository.BusScheduleRepository;
import com.busapp.busservice.model.repository.ScheduleSeatRepository;
import com.busapp.busservice.model.repository.SeatRepository;
import com.busapp.busservice.service.ScheduleSeatService;
import com.busapp.busservice.service.WebSocketService;
import com.busapp.busservice.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleSeatServiceImpl implements ScheduleSeatService {

    private final ScheduleSeatRepository scheduleSeatRepository;
    private final BusScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final ScheduleSeatMapper scheduleSeatMapper;
    private final WebSocketService webSocketService;
    private final UserUtil userUtil;

    @Override
    public List<ScheduleSeatResponse> getSeatsBySchedule(Long scheduleId) {
        log.info("[SEAT_SERVICE] Getting seats by schedule - scheduleId={}", scheduleId);
        List<ScheduleSeatResponse> seats = scheduleSeatMapper.toResponseList(
                scheduleSeatRepository.findByScheduleId(scheduleId));
        log.debug("[SEAT_SERVICE] Found {} seats for schedule {}", seats.size(), scheduleId);
        return seats;
    }

    @Override
    public ScheduleSeatDetailResponse getScheduleSeatsWithBusDetails(Long scheduleId) {
        log.info("[SEAT_SERVICE] Getting schedule seats with bus details - scheduleId={}", scheduleId);
        
        BusSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found: " + scheduleId));
        
        log.debug("[SEAT_SERVICE] Found schedule - busId={}, routeId={}", 
                schedule.getBus().getId(), schedule.getBus().getRoute().getId());
        
        List<ScheduleSeat> scheduleSeats = scheduleSeatRepository.findByScheduleId(scheduleId);
        log.debug("[SEAT_SERVICE] Found {} schedule seats", scheduleSeats.size());
        
        return scheduleSeatMapper.toDetailResponse(schedule, scheduleSeats);
    }

    @Override
    public List<ScheduleSeatResponse> getAvailableSeatsBySchedule(Long scheduleId) {
        log.info("[SEAT_SERVICE] Getting available seats - scheduleId={}", scheduleId);
        List<ScheduleSeatResponse> availableSeats = scheduleSeatMapper.toResponseList(
                scheduleSeatRepository.findByScheduleIdAndStatus(scheduleId, SeatStatus.AVAILABLE));
        log.info("[SEAT_SERVICE] Found {} available seats for schedule {}", availableSeats.size(), scheduleId);
        return availableSeats;
    }

    @Override
    public ScheduleSeatResponse getScheduleSeat(Long scheduleId, Long seatId) {
        log.debug("[SEAT_SERVICE] Getting schedule seat - scheduleId={}, seatId={}", scheduleId, seatId);
        
        ScheduleSeat scheduleSeat = scheduleSeatRepository.findByScheduleIdAndSeatId(scheduleId, seatId)
                .orElseThrow(() -> {
                    log.warn("[SEAT_SERVICE] Seat not found - scheduleId={}, seatId={}", scheduleId, seatId);
                    return new ResourceNotFoundException("Seat " + seatId + " not found for schedule " + scheduleId);
                });
        
        log.debug("[SEAT_SERVICE] Found seat - status={}, bookingId={}", 
                scheduleSeat.getStatus(), scheduleSeat.getBookingId());
        return scheduleSeatMapper.toResponse(scheduleSeat);
    }

    @Override
    @Transactional
    public ScheduleSeatResponse updateScheduleSeatStatus(Long scheduleId, Long seatId, 
                                                         SeatStatus status, Long bookingId) {
        log.info("[SEAT_SERVICE] Updating seat status - scheduleId={}, seatId={}, newStatus={}, bookingId={}", 
                scheduleId, seatId, status, bookingId);
        
        ScheduleSeat scheduleSeat = scheduleSeatRepository.findByScheduleIdAndSeatId(scheduleId, seatId)
                .orElseThrow(() -> {
                    log.error("[SEAT_SERVICE] Seat not found for status update - scheduleId={}, seatId={}", 
                            scheduleId, seatId);
                    return new ResourceNotFoundException("Seat " + seatId + " not found for schedule " + scheduleId);
                });
        
        SeatStatus oldStatus = scheduleSeat.getStatus();
        scheduleSeat.setStatus(status);
        scheduleSeat.setBookingId(bookingId);
        
        ScheduleSeatResponse response = scheduleSeatMapper.toResponse(scheduleSeatRepository.save(scheduleSeat));
        log.info("[SEAT_SERVICE] Seat status updated successfully - scheduleId={}, seatId={}, oldStatus={}, newStatus={}", 
                scheduleId, seatId, oldStatus, status);
        
        return response;
    }

    @Override
    @Transactional
    public void initializeScheduleSeats(Long scheduleId) {
        log.info("[SEAT_SERVICE] Initializing schedule seats - scheduleId={}", scheduleId);
        
        BusSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> {
                    log.error("[SEAT_SERVICE] Schedule not found for initialization - scheduleId={}", scheduleId);
                    return new ResourceNotFoundException("Schedule not found: " + scheduleId);
                });
        
        Long busId = schedule.getBus().getId();
        log.debug("[SEAT_SERVICE] Found schedule - busId={}", busId);
        
        // Get all seats for the bus
        List<Seat> busSeats = seatRepository.findByBusId(busId);
        
        if (busSeats.isEmpty()) {
            log.warn("[SEAT_SERVICE] No seats found for bus {} when initializing schedule {}", busId, scheduleId);
            return;
        }
        
        log.debug("[SEAT_SERVICE] Found {} bus seats to initialize", busSeats.size());
        
        // Create ScheduleSeat for each bus seat - all start as AVAILABLE
        List<ScheduleSeat> scheduleSeats = busSeats.stream()
                .map(seat -> ScheduleSeat.builder()
                        .schedule(schedule)
                        .seat(seat)
                        .status(SeatStatus.AVAILABLE)
                        .bookingId(null)
                        .build())
                .collect(Collectors.toList());
        
        scheduleSeatRepository.saveAll(scheduleSeats);
        log.info("[SEAT_SERVICE] Initialized {} schedule seats for schedule {}", scheduleSeats.size(), scheduleId);
    }

    @Override
    @Transactional
    public void deleteSeatsBySchedule(Long scheduleId) {
        log.info("[SEAT_SERVICE] Deleting schedule seats - scheduleId={}", scheduleId);
        scheduleSeatRepository.deleteByScheduleId(scheduleId);
        log.info("[SEAT_SERVICE] Deleted schedule seats for schedule {}", scheduleId);
    }

    @Override
    public long countAvailableSeats(Long scheduleId) {
        log.debug("[SEAT_SERVICE] Counting available seats - scheduleId={}", scheduleId);
        long count = scheduleSeatRepository.countByScheduleIdAndStatus(scheduleId, SeatStatus.AVAILABLE);
        log.debug("[SEAT_SERVICE] Available seat count - scheduleId={}, count={}", scheduleId, count);
        return count;
    }

    @Override
    public Map<SeatStatus, Long> getSeatCountsByStatus(Long scheduleId) {
        log.debug("[SEAT_SERVICE] Getting seat counts by status - scheduleId={}", scheduleId);
        
        List<ScheduleSeat> scheduleSeats = scheduleSeatRepository.findByScheduleId(scheduleId);
        
        Map<SeatStatus, Long> counts = scheduleSeats.stream()
                .collect(Collectors.groupingBy(
                        ScheduleSeat::getStatus,
                        Collectors.counting()
                ));
        
        log.debug("[SEAT_SERVICE] Seat counts by status - scheduleId={}, counts={}", scheduleId, counts);
        return counts;
    }

    @Override
    public boolean areSeatsAvailable(Long scheduleId, List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            log.warn("[SEAT_SERVICE] Seat availability check failed - empty seat list");
            return false;
        }
        
        log.info("[SEAT_SERVICE] Checking seat availability - scheduleId={}, seatIds={}", scheduleId, seatIds);

        List<ScheduleSeat> scheduleSeats = scheduleSeatRepository
                .findByScheduleIdAndSeatIdIn(scheduleId, seatIds);
        
        // Check if all requested seats exist and are available
        if (scheduleSeats.size() != seatIds.size()) {
            log.warn("[SEAT_SERVICE] Not all seats found - scheduleId={}, requested={}, found={}", 
                    scheduleId, seatIds.size(), scheduleSeats.size());
            return false;
        }
        log.info("current user id :{}",userUtil.getCurrentUserId());
        boolean allAvailable = scheduleSeats.stream()
                .allMatch(ss -> ss.getStatus() == SeatStatus.AVAILABLE ||
                        (ss.getStatus() == SeatStatus.PENDING && 
                         ss.getPendingUserId() != null && 
                         ss.getPendingUserId().equals(userUtil.getCurrentUserId())));
        
        if (!allAvailable) {
                List<Long> unavailableSeats = scheduleSeats.stream()
                        .filter(ss ->
                                ss.getStatus() == SeatStatus.BOOKED ||
                                        (ss.getStatus() == SeatStatus.PENDING &&
                                                (ss.getPendingUserId() == null || 
                                             !ss.getPendingUserId().equals(userUtil.getCurrentUserId())))
                        )
                        .map(ss -> ss.getSeat().getId())
                        .collect(Collectors.toList());
            log.warn("[SEAT_SERVICE] Some seats not available - scheduleId={}, unavailableSeats={}", 
                    scheduleId, unavailableSeats);
        } else {
            log.info("[SEAT_SERVICE] All seats available - scheduleId={}, seatCount={}", 
                    scheduleId, seatIds.size());
        }
        
        return allAvailable;
    }

    @Override
    @Transactional
    public void bookSeats(Long scheduleId, List<Long> seatIds, Long bookingId) {
        log.info("[SEAT_SERVICE] Booking seats - scheduleId={}, seatIds={}, bookingId={}", 
                scheduleId, seatIds, bookingId);
        
        if (seatIds == null || seatIds.isEmpty()) {
            log.error("[SEAT_SERVICE] Booking failed - empty seat list");
            throw new BadRequestException("Seat IDs cannot be empty");
        }
        
        Long currentUserId = userUtil.getCurrentUserId();
        
        // ⚡ PERFORMANCE: Use batch update instead of fetching and updating individually
        log.debug("[SEAT_SERVICE] Executing batch seat booking query");
        int updatedCount = scheduleSeatRepository.batchBookSeats(scheduleId, seatIds, bookingId, currentUserId);
        
        if (updatedCount != seatIds.size()) {
            log.error("[SEAT_SERVICE] Booking failed - not all seats updated. Requested={}, Updated={}", 
                    seatIds.size(), updatedCount);
            throw new BadRequestException("Some seats are not available for booking");
        }
        
        log.info("[SEAT_SERVICE] Successfully booked {} seats - scheduleId={}, bookingId={}", 
                seatIds.size(), scheduleId, bookingId);
        
        // ⚡ PERFORMANCE: Broadcast WebSocket events asynchronously
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                log.debug("[SEAT_SERVICE] Broadcasting SEAT_BOOKED events via WebSocket (async)");
                // Fetch seat details only for WebSocket broadcasting (async)
                List<ScheduleSeat> bookedSeats = scheduleSeatRepository
                        .findByScheduleIdAndSeatIdIn(scheduleId, seatIds);
                bookedSeats.forEach(ss -> broadcastSeatEvent("SEAT_BOOKED", scheduleId, ss, bookingId));
            } catch (Exception e) {
                log.error("[SEAT_SERVICE] Failed to broadcast WebSocket events: {}", e.getMessage());
            }
        });
    }

    @Override
    @Transactional
    public void releaseSeats(Long bookingId) {
        log.info("[SEAT_SERVICE] Releasing seats - bookingId={}", bookingId);
        
        List<ScheduleSeat> scheduleSeats = scheduleSeatRepository.findByBookingId(bookingId);
        
        if (scheduleSeats.isEmpty()) {
            log.warn("[SEAT_SERVICE] No schedule seats found for booking {}", bookingId);
            return;
        }
        
        log.debug("[SEAT_SERVICE] Found {} seats to release", scheduleSeats.size());
        
        scheduleSeats.forEach(ss -> {
            log.debug("[SEAT_SERVICE] Releasing seat - scheduleId={}, seatId={}, oldStatus={}", 
                    ss.getSchedule().getId(), ss.getSeat().getId(), ss.getStatus());
            ss.setStatus(SeatStatus.AVAILABLE);
            ss.setBookingId(null);
        });
        
        scheduleSeatRepository.saveAll(scheduleSeats);
        log.info("[SEAT_SERVICE] Successfully released {} seats for booking {}", scheduleSeats.size(), bookingId);
        
        // Broadcast seat availability updates via WebSocket
        log.debug("[SEAT_SERVICE] Broadcasting SEAT_RELEASED events via WebSocket");
        scheduleSeats.forEach(ss -> broadcastSeatEvent("SEAT_RELEASED", ss.getSchedule().getId(), ss, null));
    }
    
    // ─────────────────────────────────────────────
    // WebSocket Broadcasting Helper
    // ─────────────────────────────────────────────
    
    private void broadcastSeatEvent(String type, Long scheduleId, ScheduleSeat scheduleSeat, Long bookingId) {
        try {
            log.debug("[WEBSOCKET] Preparing to broadcast seat event - type={}, scheduleId={}, seatId={}, bookingId={}", 
                    type, scheduleId, scheduleSeat.getSeat().getId(), bookingId);
            
            com.busapp.busservice.dto.event.SeatAvailabilityEvent event = 
                com.busapp.busservice.dto.event.SeatAvailabilityEvent.builder()
                    .type(type)
                    .scheduleId(scheduleId)
                    .seatId(scheduleSeat.getSeat().getId())
                    .seatNumber(scheduleSeat.getSeat().getSeatNumber())
                    .bookingId(bookingId)
                    .status(scheduleSeat.getStatus().name())
                    .timestamp(java.time.LocalDateTime.now())
                    .build();
            
            webSocketService.broadcastSeatAvailability(event);
            log.info("[WEBSOCKET] Successfully broadcasted seat event - type={}, scheduleId={}, seatId={}", 
                    type, scheduleId, scheduleSeat.getSeat().getId());
        } catch (Exception e) {
            log.error("[WEBSOCKET] Failed to broadcast seat event - type={}, scheduleId={}, seatId={}, error={}", 
                    type, scheduleId, scheduleSeat.getSeat().getId(), e.getMessage(), e);
        }
    }
}
