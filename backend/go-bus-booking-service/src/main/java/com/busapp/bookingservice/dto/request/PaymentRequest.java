package com.busapp.bookingservice.dto.request;

import com.busapp.bookingservice.model.enums.Currency;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {
    @NotNull
    private Long bookingId;
    @NotNull
    private PaymentMethodType paymentMethodType;
    private UUID walletTransactionId;
    private String transactionId;
    @NotNull
    private Double amount;
    @NotNull
    private PaymentStatus paymentStatus;
    @NotNull
    private Currency currency;
    @NotNull
    private String hash;
}
