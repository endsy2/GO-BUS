package com.busapp.bookingservice.repository;

import com.busapp.bookingservice.model.Booking;
import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByScheduleId(Long scheduleId);
    List<Booking> findByBookingStatus(BookingStatus status);
    List<Booking> findByPaymentStatus(PaymentStatus paymentStatus);
    List<Booking> findByUserIdAndBookingStatus(Long userId, BookingStatus status);

    // Report queries
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    Long countBookingsByDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b.bookingStatus, COUNT(b) FROM Booking b " +
            "WHERE b.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY b.bookingStatus")
    List<Object[]> countBookingsByStatusAndDateRange(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    List<Booking> findBookingsByDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b.scheduleId, COUNT(b), SUM(b.totalAmount) FROM Booking b " +
            "WHERE b.bookingStatus = 'CONFIRMED' AND b.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY b.scheduleId")
    List<Object[]> getRevenueBySchedule(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b.scheduleId, b.bookingStatus, COUNT(b) FROM Booking b " +
            "WHERE b.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY b.scheduleId, b.bookingStatus")
    List<Object[]> getBookingStatusCountsBySchedule(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b.scheduleId, COUNT(b) FROM Booking b " +
            "WHERE b.bookingStatus = 'CONFIRMED' AND b.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY b.scheduleId ORDER BY COUNT(b) DESC")
    List<Object[]> getPopularSchedules(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // Bus-specific queries for reports
    Long countByScheduleIdAndBookingStatus(Long scheduleId, BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.scheduleId = :scheduleId AND b.bookingStatus = 'CONFIRMED'")
    Long countConfirmedBookingsByScheduleId(@Param("scheduleId") Long scheduleId);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b " +
            "WHERE b.scheduleId = :scheduleId AND b.bookingStatus = 'CONFIRMED' " +
            "AND b.createdAt BETWEEN :startDate AND :endDate")
    Double calculateRevenueByScheduleIdWithDate(@Param("scheduleId") Long scheduleId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b " +
            "WHERE b.scheduleId = :scheduleId AND b.bookingStatus = 'CONFIRMED' ")
    Double calculateRevenueByScheduleId(@Param("scheduleId") Long scheduleId);

    // Customer analytics queries
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.userId = :userId " +
            "AND b.createdAt BETWEEN :startDate AND :endDate")
    Long countByUserIdAndDateRange(@Param("userId") Long userId,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.userId = :userId " +
            "AND b.bookingStatus = 'CONFIRMED' " +
            "AND b.createdAt BETWEEN :startDate AND :endDate")
    Double calculateRevenueByUserId(@Param("userId") Long userId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT " +
            "b.user_id as userId, " +
            "COUNT(b.id) as totalBookings, " +
            "SUM(b.total_amount) as totalSpent, " +
            "MAX(b.created_at) as lastBookingDate " +
            "FROM booking_service.\"Booking\" b " +
            "WHERE b.booking_status = 'CONFIRMED' " +
            "AND b.created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY b.user_id " +
            "ORDER BY totalBookings DESC",
            nativeQuery = true)
    List<Map<String, Object>> getActiveUsersStats(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT " +
            "b.user_id as userId, " +
            "COUNT(b.id) as totalBookings, " +
            "COUNT(CASE WHEN b.booking_status = 'CONFIRMED' THEN 1 END) as confirmedBookings, " +
            "SUM(b.total_amount) as totalSpent, " +
            "MIN(b.created_at) as firstBookingDate, " +
            "MAX(b.created_at) as lastBookingDate " +
            "FROM booking_service.\"Booking\" b " +
            "WHERE b.created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY b.user_id " +
            "ORDER BY totalBookings DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Map<String, Object>> getFrequentTravelers(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate,
                                                   @Param("limit") Integer limit);

    @Query(value = "SELECT " +
            "EXTRACT(DOW FROM b.created_at) as dayOfWeek, " +
            "COUNT(b.id) as totalBookings, " +
            "COUNT(DISTINCT b.user_id) as uniqueCustomers, " +
            "SUM(b.total_amount) as totalRevenue, " +
            "AVG(b.total_amount) as averageBookingValue " +
            "FROM booking_service.\"Booking\" b " +
            "WHERE b.booking_status = 'CONFIRMED' " +
            "AND b.created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY dayOfWeek " +
            "ORDER BY dayOfWeek",
            nativeQuery = true)
    List<Map<String, Object>> getBookingPatternsByDayOfWeek(@Param("startDate") LocalDateTime startDate,
                                                            @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT " +
            "EXTRACT(HOUR FROM b.created_at) as hour, " +
            "COUNT(b.id) as totalBookings, " +
            "COUNT(DISTINCT b.user_id) as uniqueCustomers, " +
            "SUM(b.total_amount) as totalRevenue, " +
            "AVG(b.total_amount) as averageBookingValue " +
            "FROM booking_service.\"Booking\" b " +
            "WHERE b.booking_status = 'CONFIRMED' " +
            "AND b.created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY hour " +
            "ORDER BY hour",
            nativeQuery = true)
    List<Map<String, Object>> getBookingPatternsByHour(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Summary booking stats for one user — 3 columns only.
     * LEFT JOIN to Ticket is kept for activeTickets; all other aggregates removed.
     *
     * Result tuple positions:
     *   [0] totalBookings  (Long)
     *   [1] totalSpent     (BigDecimal)
     *   [2] activeTickets  (Long)
     */
    @Query(value = """
    SELECT 
        COUNT(b.id) AS total_bookings,
        COALESCE(
            SUM(
                CASE 
                    WHEN b.booking_status = 'CONFIRMED' 
                    THEN b.total_amount 
                    ELSE 0 
                END
            ), 
        0) AS total_spent,
        COUNT(
            CASE 
                WHEN b.booking_status = 'CONFIRMED'
                     AND b.departure_at > NOW()
                     AND t.id IS NOT NULL
                THEN 1
            END
        ) AS active_tickets
    FROM booking_service.booking b
    LEFT JOIN booking_service.ticket t 
        ON t.booking_id = b.id
    WHERE b.user_id = :userId
      AND b.is_deleted = false
    """, nativeQuery = true)
    List<Object[]> getUserLifetimeStats(@Param("userId") Long userId);

    // Soft delete queries
    @Query("SELECT b FROM Booking b WHERE b.isDeleted = false")
    List<Booking> findAllActive();

    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.isDeleted = false")
    List<Booking> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b WHERE b.scheduleId = :scheduleId AND b.isDeleted = false")
    List<Booking> findActiveByScheduleId(@Param("scheduleId") Long scheduleId);

    // Dashboard statistics queries
    Long countByBookingStatusInAndIsDeleted(List<BookingStatus> statuses, Boolean isDeleted);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b " +
            "WHERE b.bookingStatus = :bookingStatus " +
            "AND b.paymentStatus = :paymentStatus " +
            "AND b.createdAt BETWEEN :startDate AND :endDate " +
            "AND b.isDeleted = false")
    java.math.BigDecimal sumTotalAmountByBookingStatusAndPaymentStatusAndCreatedAtBetween(
            @Param("bookingStatus") BookingStatus bookingStatus,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    Long countByCreatedAtBetweenAndIsDeleted(LocalDateTime startDate, LocalDateTime endDate, Boolean isDeleted);

    Long countByPaymentStatusAndIsDeleted(PaymentStatus paymentStatus, Boolean isDeleted);

    Long countByBookingStatusAndIsDeleted(BookingStatus bookingStatus, Boolean isDeleted);

    // Booking velocity trend analysis - grouped by date only (seat type fetched via Feign)
    @Query(value = "SELECT " +
            "DATE(b.\"created_at\") as booking_date, " +
            "bs.\"seat_id\" as seat_id, " +
            "b.\"total_amount\" as booking_amount " +
            "FROM booking_service.\"booking\" b " +
            "JOIN booking_service.\"booking_seat\" bs ON bs.\"booking_id\" = b.id " +
            "WHERE b.\"booking_status\" = 'CONFIRMED' " +
            "AND b.\"is_deleted\" = false " +
            "AND b.\"created_at\" BETWEEN :startDate AND :endDate " +
            "ORDER BY booking_date ASC",
            nativeQuery = true)
    List<Object[]> getBookingVelocityData(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
