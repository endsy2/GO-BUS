package com.busapp.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStreamResponse {
    private BigDecimal weeklyTotal;
    private List<PaymentMethodRevenue> paymentMethods;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodRevenue {
        private String method;
        private BigDecimal amount;
        private Double percentage;
        private Long transactionCount;
    }
}
