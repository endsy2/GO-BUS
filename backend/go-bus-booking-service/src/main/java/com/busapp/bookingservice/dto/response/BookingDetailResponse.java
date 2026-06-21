package com.busapp.bookingservice.dto.response;

import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import com.busapp.bookingservice.model.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Rich booking detail returned when the user clicks into a single booking.
 * Includes all nested data (seats, payments, ticket, promo) and cross-service
 * enrichment (user info from user-service, schedule + bus info from bus-service).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailResponse {

    private Long              id;
    private BookingStatus     bookingStatus;
    private RefundStatus      refundStatus;
    private PaymentStatus     paymentStatus;
    private PaymentMethodType paymentMethod;
    private Double            totalAmount;
    private LocalDateTime     createdAt;
    private String            phoneNumber;

    /** Snapshot of the user who made this booking (from user-service). */
    private UserSnapshot user;

    /** Snapshot of the schedule this booking is for (from bus-service). */
    private ScheduleSnapshot schedule;

    /** Full promo details — null if no promo was applied. */
    private PromoCodeResponse promo;

    /** One entry per seat reserved in this booking. */
    private List<SeatDetail> seats;

    /** Payment record for this booking. */
    private PaymentResponse payment;

    /** Issued ticket — null if the booking is not yet confirmed/paid. */
    private TicketResponse ticket;

    // ── Nested snapshot types ─────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSnapshot {
        private Long   id;
        private String fullName;
        private String email;
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleSnapshot {
        private Long   id;
        private Long   busId;
        private String busNumber;
        private String busType;
        private LocalDateTime departureDateTime;
        private LocalDateTime arrivalDateTime;
        private Double price;
        private String origin;
        private String destination;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatDetail {
        private Long   seatId;
        private String seatNumber;
        private String seatType;
        private Long   passengerNumber;
    }
}
