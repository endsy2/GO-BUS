package com.busapp.busservice.service;

import com.busapp.busservice.dto.ScheduleSeatDetailResponse;
import com.busapp.busservice.dto.ScheduleSeatResponse;
import com.busapp.busservice.model.enums.SeatStatus;

import java.util.List;
import java.util.Map;

public interface ScheduleSeatService {
    
    /**
     * Get all seats for a specific schedule with their availability status
     */
    List<ScheduleSeatResponse> getSeatsBySchedule(Long scheduleId);
    
    /**
     * Get detailed schedule seat information including full bus details
     */
    ScheduleSeatDetailResponse getScheduleSeatsWithBusDetails(Long scheduleId);
    
    /**
     * Get available seats for a specific schedule
     */
    List<ScheduleSeatResponse> getAvailableSeatsBySchedule(Long scheduleId);
    
    /**
     * Get a specific seat for a schedule
     */
    ScheduleSeatResponse getScheduleSeat(Long scheduleId, Long seatId);
    
    /**
     * Update seat status for a specific schedule
     */
    ScheduleSeatResponse updateScheduleSeatStatus(Long scheduleId, Long seatId, SeatStatus status, Long bookingId);
    
    /**
     * Initialize schedule seats when a new schedule is created
     */
    void initializeScheduleSeats(Long scheduleId);
    
    /**
     * Delete all schedule seats for a schedule (used before deleting a schedule).
     */
    void deleteSeatsBySchedule(Long scheduleId);
    
    /**
     * Count available seats for a schedule
     */
    long countAvailableSeats(Long scheduleId);
    
    /**
     * Get seat counts by status for a schedule
     */
    Map<SeatStatus, Long> getSeatCountsByStatus(Long scheduleId);
    
    /**
     * Check if seats are available for booking
     */
    boolean areSeatsAvailable(Long scheduleId, List<Long> seatIds);
    
    /**
     * Book seats for a schedule
     */
    void bookSeats(Long scheduleId, List<Long> seatIds, Long bookingId);
    
    /**
     * Release seats (cancel booking)
     */
    void releaseSeats(Long bookingId);
}
