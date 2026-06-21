package com.busapp.bookingservice.dto.request;

import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingFilterRequest {
    private Long userId;
    private Long scheduleId;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private PaymentMethodType paymentMethod;
    private Double minAmount;
    private Double maxAmount;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private Boolean refund;
}
