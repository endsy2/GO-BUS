package com.busapp.bookingservice.dto.request;

import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingFilterRequest {
    private String username;
    private Long userId;
    private Long scheduleId;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private PaymentMethodType paymentMethod;
    private Boolean refund;
    /** Filter by departure date range (inclusive). Maps to Booking.departureAt. */
    private LocalDate departureFrom;
    private LocalDate departureTo;
    /** Filter by booking creation date range (inclusive). Maps to Booking.createdAt. */
    private LocalDate createdFrom;
    private LocalDate createdTo;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 20;
}
