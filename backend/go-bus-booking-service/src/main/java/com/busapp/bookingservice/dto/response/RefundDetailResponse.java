package com.busapp.bookingservice.dto.response;

import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import com.busapp.bookingservice.model.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detailed refund response with complete booking, schedule, route, and wallet information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundDetailResponse {
    
    // ── Refund Information ────────────────────────────────────────────────────
    private Long id;
    private BigDecimal amount;
    private String reason;
    private RefundStatus status;
    private String adminNote;
    private Long processedBy;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // ── Booking Information ───────────────────────────────────────────────────
    private BookingSnapshot booking;
    
    // ── User/Wallet Information ───────────────────────────────────────────────
    private UserSnapshot user;
    private WalletSnapshot wallet;
    
    // ── Schedule Information ──────────────────────────────────────────────────
    private ScheduleSnapshot schedule;
    
    // ── Route Information ─────────────────────────────────────────────────────
    private RouteSnapshot route;
    
    // ── Nested Snapshot Types ─────────────────────────────────────────────────
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingSnapshot {
        private Long id;
        private BookingStatus bookingStatus;
        private PaymentStatus paymentStatus;
        private PaymentMethodType paymentMethod;
        private BigDecimal totalAmount;
        private String phoneNumber;
        private LocalDateTime createdAt;
        private List<SeatDetail> seats;
        private PromoCodeResponse promo;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSnapshot {
        private Long id;
        private String userName;
        private String fullName;
        private String email;
        private String phone;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletSnapshot {
        private String walletId;
        private Double balance;
        private String currency;
        private String status;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleSnapshot {
        private Long id;
        private LocalDateTime departureDateTime;
        private LocalDateTime arrivalDateTime;
        private Double price;
        private Integer availableSeats;
        private BusSnapshot bus;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusSnapshot {
        private Long id;
        private String busNumber;
        private String busType;
        private Integer totalSeats;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteSnapshot {
        private Long id;
        private String routeName;
        private String origin;
        private String destination;
        private Double distance;
        private Integer estimatedDuration;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatDetail {
        private Long seatId;
        private Long passengerNumber;
    }
}
