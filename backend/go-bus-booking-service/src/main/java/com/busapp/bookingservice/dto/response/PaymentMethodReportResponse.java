package com.busapp.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodReportResponse {
    private String paymentMethod;
    private Long totalTransactions;
    private Long successfulTransactions;
    private Long failedTransactions;
    private Double totalAmount;
    private Double averageTransactionValue;
    private Double percentageOfTotal;
}
