package com.busapp.bookingservice.repository;

import com.busapp.bookingservice.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    List<BookingSeat> findByBookingId(Long bookingId);

    @Query("SELECT bs FROM BookingSeat bs WHERE bs.booking.id IN :bookingIds")
    List<BookingSeat> findByBookingIdIn(@Param("bookingIds") List<Long> bookingIds);
    List<BookingSeat> findBySeatId(Long seatId);
    boolean existsByBookingIdAndSeatId(Long bookingId, Long seatId);

    @Query("SELECT COUNT(DISTINCT bs.seatId) FROM BookingSeat bs " +
           "JOIN bs.booking b WHERE b.scheduleId = :scheduleId " +
           "AND b.bookingStatus = 'CONFIRMED'")
    Long countBookedSeatsBySchedule(@Param("scheduleId") Long scheduleId);

    @Query("SELECT COUNT(bs) FROM BookingSeat bs " +
           "JOIN bs.booking b WHERE b.scheduleId = :scheduleId " +
           "AND b.bookingStatus = 'CONFIRMED' " +
           "AND b.createdAt BETWEEN :startDate AND :endDate")
    Long countTicketsSoldByScheduleAndDateRange(@Param("scheduleId") Long scheduleId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

}
