package com.busapp.userservice.dto.response;

import com.busapp.userservice.model.enums.PaymentMethodType;
import com.busapp.userservice.model.enums.TopUpStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUpResponse {
    private Long              id;
    private Long              userId;
    private Double            amount;
    private PaymentMethodType paymentMethod;
    private TopUpStatus       status;
    private String            paymentGateway;
    private String            transactionId;
    private LocalDateTime     completedAt;
    private LocalDateTime     createdAt;
}
