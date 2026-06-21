package com.busapp.bookingservice.dto.response;

import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long bookingId;
    private Double amount;
    private PaymentMethodType method;
    private String transactionId;
    private PaymentStatus status;
    private LocalDateTime paidAt;
    private UUID walletTransactionId;
}
