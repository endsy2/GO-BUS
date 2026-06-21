package com.busapp.bookingservice.repository;

import com.busapp.bookingservice.model.Payment;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);

    @Query("SELECT p FROM Payment p WHERE p.booking.id IN :bookingIds")
    List<Payment> findByBookingIdIn(@Param("bookingIds") List<Long> bookingIds);
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByStatus(PaymentStatus status);
    
    /**
     * Get revenue aggregated by payment method for a date range
     * Returns: [method, totalAmount, transactionCount]
     */
    @Query("""
        SELECT p.method, 
               SUM(p.amount), 
               COUNT(p.id)
        FROM Payment p
        WHERE p.status = com.busapp.bookingservice.model.enums.PaymentStatus.SUCCESS
          AND p.paidAt IS NOT NULL
          AND p.paidAt BETWEEN :fromDate AND :toDate
        GROUP BY p.method
        ORDER BY SUM(p.amount) DESC
        """)
    List<Object[]> getRevenueByPaymentMethod(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );
}
