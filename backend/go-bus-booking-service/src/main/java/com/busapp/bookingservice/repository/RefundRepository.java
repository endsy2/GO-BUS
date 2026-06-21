package com.busapp.bookingservice.repository;

import com.busapp.bookingservice.model.Refund;
import com.busapp.bookingservice.model.enums.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long>, JpaSpecificationExecutor<Refund> {

    /**
     * Find refund by booking ID
     */
    Optional<Refund> findByBookingId(Long bookingId);

    /**
     * Check if refund exists for booking
     */
    boolean existsByBookingId(Long bookingId);

    /**
     * Find all refunds by user ID (through booking)
     */
    @Query("SELECT r FROM Refund r WHERE r.booking.userId = :userId")
    Page<Refund> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find refunds by user ID and status
     */
    @Query("SELECT r FROM Refund r WHERE r.booking.userId = :userId AND r.status = :status")
    Page<Refund> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") RefundStatus status, Pageable pageable);

    /**
     * Find refunds by status
     */
    Page<Refund> findByStatus(RefundStatus status, Pageable pageable);

    /**
     * Find refunds by status and date range
     */
    @Query("SELECT r FROM Refund r WHERE r.status = :status " +
           "AND r.createdAt BETWEEN :fromDate AND :toDate")
    Page<Refund> findByStatusAndDateRange(
            @Param("status") RefundStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    /**
     * Count refunds by status
     */
    long countByStatus(RefundStatus status);

    /**
     * Get refund statistics
     */
    @Query("SELECT COUNT(r) FROM Refund r WHERE r.status = :status " +
           "AND r.createdAt BETWEEN :fromDate AND :toDate")
    long countByStatusAndDateRange(
            @Param("status") RefundStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    /**
     * Get total refund amount by status
     */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r WHERE r.status = :status " +
           "AND r.createdAt BETWEEN :fromDate AND :toDate")
    Double getTotalRefundAmountByStatusAndDateRange(
            @Param("status") RefundStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    /**
     * Get average refund amount
     */
    @Query("SELECT COALESCE(AVG(r.amount), 0) FROM Refund r WHERE r.status = :status " +
           "AND r.createdAt BETWEEN :fromDate AND :toDate")
    Double getAverageRefundAmountByStatusAndDateRange(
            @Param("status") RefundStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    /**
     * Get total refund amount by status (all time)
     */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r WHERE r.status = :status")
    BigDecimal getTotalRefundAmountByStatus(@Param("status") RefundStatus status);

}
