package com.busapp.bookingservice.dto.request;

import com.busapp.bookingservice.model.enums.PaymentMethodType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {

    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;

    @NotEmpty(message = "At least one seat ID is required")
    private List<Long> seatIds;

    /** Optional promo code to apply. */
    private String promoCode;

    /** Phone number for booking contact. */
    private String phoneNumber;

//    @NotNull(message = "Payment method is required")
//    private PaymentMethodType paymentMethod;
}