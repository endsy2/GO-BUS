package com.busapp.busservice.controller;

import com.busapp.busservice.dto.ApiResponse;
import com.busapp.busservice.dto.ScheduleSeatDetailResponse;
import com.busapp.busservice.dto.ScheduleSeatResponse;
import com.busapp.busservice.service.ScheduleSeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/schedule-seats")
@RequiredArgsConstructor
public class ScheduleSeatController {

    private final ScheduleSeatService scheduleSeatService;

    /**
     * Get all seats for a specific schedule with their availability and full bus details
     * GET /api/schedule-seats/schedule/{scheduleId}
     */
    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleSeatDetailResponse>> getSeatsBySchedule(
            @PathVariable Long scheduleId) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Schedule seats retrieved successfully",
                scheduleSeatService.getScheduleSeatsWithBusDetails(scheduleId)));
    }

    /**
     * Get only available seats for a specific schedule
     * GET /api/schedule-seats/schedule/{scheduleId}/available
     */
    @GetMapping("/schedule/{scheduleId}/available")
    public ResponseEntity<ApiResponse<List<ScheduleSeatResponse>>> getAvailableSeatsBySchedule(
            @PathVariable Long scheduleId) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Available seats retrieved successfully",
                scheduleSeatService.getAvailableSeatsBySchedule(scheduleId)));
    }

    /**
     * Get a specific seat for a schedule
     * GET /api/schedule-seats/schedule/{scheduleId}/seat/{seatId}
     */
    @GetMapping("/schedule/{scheduleId}/seat/{seatId}")
    public ResponseEntity<ApiResponse<ScheduleSeatResponse>> getScheduleSeat(
            @PathVariable Long scheduleId,
            @PathVariable Long seatId) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Schedule seat retrieved successfully",
                scheduleSeatService.getScheduleSeat(scheduleId, seatId)));
    }

    /**
     * Count available seats for a schedule
     * GET /api/schedule-seats/schedule/{scheduleId}/available/count
     */
    @GetMapping("/schedule/{scheduleId}/available/count")
    public ResponseEntity<ApiResponse<Long>> countAvailableSeats(
            @PathVariable Long scheduleId) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Available seats count retrieved successfully",
                scheduleSeatService.countAvailableSeats(scheduleId)));
    }

    /**
     * Check if specific seats are available for a schedule
     * POST /api/schedule-seats/schedule/{scheduleId}/check-availability
     */
    @PostMapping("/schedule/{scheduleId}/check-availability")
    public ResponseEntity<ApiResponse<Boolean>> checkSeatsAvailability(
            @PathVariable Long scheduleId,
            @RequestBody List<Long> seatIds) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Seat availability checked",
                scheduleSeatService.areSeatsAvailable(scheduleId, seatIds)));
    }
    
    /**
     * Book seats for a schedule (Internal API for booking-service)
     * POST /api/schedule-seats/schedule/{scheduleId}/book?bookingId={bookingId}
     */
    @PostMapping("/schedule/{scheduleId}/book")
    public ResponseEntity<ApiResponse<Void>> bookSeats(
            @PathVariable Long scheduleId,
            @RequestParam Long bookingId,
            @RequestBody List<Long> seatIds) {
        scheduleSeatService.bookSeats(scheduleId, seatIds, bookingId);
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Seats booked successfully",
                null));
    }
    
    /**
     * Release seats for a booking (Internal API for booking-service)
     * POST /api/schedule-seats/booking/{bookingId}/release
     */
    @PostMapping("/booking/{bookingId}/release")
    public ResponseEntity<ApiResponse<Void>> releaseSeats(@PathVariable Long bookingId) {
        scheduleSeatService.releaseSeats(bookingId);
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Seats released successfully",
                null));
    }
}
