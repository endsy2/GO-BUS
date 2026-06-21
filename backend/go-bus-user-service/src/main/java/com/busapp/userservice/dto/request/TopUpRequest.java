package com.busapp.userservice.dto.request;

import com.busapp.userservice.model.enums.PaymentMethodType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUpRequest {
    @NotNull
    @Positive
    private Double            amount;

    @NotNull
    private PaymentMethodType paymentMethod;

    private String            paymentGateway;
    private String            transactionId;
}
