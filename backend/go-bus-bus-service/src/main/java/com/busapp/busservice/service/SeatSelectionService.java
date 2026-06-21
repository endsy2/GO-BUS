package com.busapp.busservice.service;

/**
 * Service for handling real-time seat selection/deselection
 */
public interface SeatSelectionService {
    
    /**
     * Mark a seat as PENDING when user selects it
     * @param scheduleId Schedule ID
     * @param seatId Seat ID
     * @param userId User ID who is selecting the seat
     */
    void selectSeat(Long scheduleId, Long seatId, Long userId);
    
    /**
     * Mark a seat as AVAILABLE when user deselects it
     * @param scheduleId Schedule ID
     * @param seatId Seat ID
     * @param userId User ID who is deselecting the seat
     */
    void deselectSeat(Long scheduleId, Long seatId, Long userId);
    
    /**
     * Release expired pending seats (called by scheduled task)
     */
    void releaseExpiredPendingSeats();
}
