package com.busapp.bookingservice.dto.response;

import com.busapp.bookingservice.client.BusClient;
import com.busapp.bookingservice.client.UserClient;
import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import com.busapp.bookingservice.model.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
public class BookingResponse {
    private Long              id;
    private String            fullName;
    private String            destination;
    private Long              scheduleId;
    private BookingStatus     bookingStatus;
    private BigDecimal        totalAmount;
    private Long              promoId;
    private PaymentStatus     paymentStatus;
    private PaymentMethodType paymentMethod;
    private RefundStatus      refundStatus;  // Latest refund status (null if no refunds)
    private LocalDateTime     createdAt;
    private LocalDateTime     departureAt;
    private Boolean           isDeleted;
    private LocalDateTime     deletedAt;
    private String            phoneNumber;
}
