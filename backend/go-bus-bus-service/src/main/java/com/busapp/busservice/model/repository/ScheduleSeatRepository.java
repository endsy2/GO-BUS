package com.busapp.busservice.model.repository;

import com.busapp.busservice.model.ScheduleSeat;
import com.busapp.busservice.model.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleSeatRepository extends JpaRepository<ScheduleSeat, Long> {
    
    /**
     * Find all schedule seats for a specific schedule
     */
    List<ScheduleSeat> findByScheduleId(Long scheduleId);
    
    /**
     * Find all schedule seats for a specific schedule with a given status
     */
    List<ScheduleSeat> findByScheduleIdAndStatus(Long scheduleId, SeatStatus status);
    
    /**
     * Find a specific seat for a specific schedule
     */
    Optional<ScheduleSeat> findByScheduleIdAndSeatId(Long scheduleId, Long seatId);
    
    /**
     * Count seats by schedule and status
     */
    long countByScheduleIdAndStatus(Long scheduleId, SeatStatus status);
    
    /**
     * Find schedule seat by booking ID
     */
    List<ScheduleSeat> findByBookingId(Long bookingId);
    
    /**
     * Check if a seat exists for a schedule
     */
    boolean existsByScheduleIdAndSeatId(Long scheduleId, Long seatId);
    
    /**
     * Delete all schedule seats for a specific schedule
     */
    @Modifying
    @Query("DELETE FROM ScheduleSeat ss WHERE ss.schedule.id = :scheduleId")
    void deleteByScheduleId(@Param("scheduleId") Long scheduleId);
    
    /**
     * Delete all schedule seats for a specific seat
     */
    @Modifying
    @Query("DELETE FROM ScheduleSeat ss WHERE ss.seat.id = :seatId")
    void deleteBySeatId(@Param("seatId") Long seatId);
    
    /**
     * Find schedule seats by schedule ID and seat IDs
     */
    @Query("SELECT ss FROM ScheduleSeat ss WHERE ss.schedule.id = :scheduleId AND ss.seat.id IN :seatIds")
    List<ScheduleSeat> findByScheduleIdAndSeatIdIn(@Param("scheduleId") Long scheduleId, 
                                                     @Param("seatIds") List<Long> seatIds);
    
    /**
     * ⚡ PERFORMANCE: Count available seats in a single query
     * Counts seats that are either AVAILABLE or PENDING by the current user
     */
    @Query("SELECT COUNT(ss) FROM ScheduleSeat ss " +
           "WHERE ss.schedule.id = :scheduleId " +
           "AND ss.seat.id IN :seatIds " +
           "AND (ss.status = 'AVAILABLE' OR " +
           "     (ss.status = 'PENDING' AND ss.pendingUserId = :userId))")
    long countAvailableSeats(@Param("scheduleId") Long scheduleId,
                             @Param("seatIds") List<Long> seatIds,
                             @Param("userId") Long userId);
    
    /**
     * ⚡ PERFORMANCE: Batch update seat status to BOOKED
     * Updates multiple seats in a single query instead of individual updates
     */
    @Modifying
    @Query("UPDATE ScheduleSeat ss " +
           "SET ss.status = 'BOOKED', ss.bookingId = :bookingId, ss.pendingUserId = null, ss.pendingAt = null " +
           "WHERE ss.schedule.id = :scheduleId " +
           "AND ss.seat.id IN :seatIds " +
           "AND (ss.status = 'AVAILABLE' OR " +
           "     (ss.status = 'PENDING' AND ss.pendingUserId = :userId))")
    int batchBookSeats(@Param("scheduleId") Long scheduleId,
                       @Param("seatIds") List<Long> seatIds,
                       @Param("bookingId") Long bookingId,
                       @Param("userId") Long userId);
    
    /**
     * Find pending seats that expired before a given time
     */
    List<ScheduleSeat> findByStatusAndPendingAtBefore(SeatStatus status, java.time.LocalDateTime expiryTime);
}
