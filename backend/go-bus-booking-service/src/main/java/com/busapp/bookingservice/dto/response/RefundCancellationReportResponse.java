package com.busapp.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundCancellationReportResponse {
    private String period;
    private Long totalCancellations;
    private Double totalRefundAmount;
    private Double averageRefundAmount;
    private Long userInitiatedCancellations;
    private Long systemCancellations;
    private Double cancellationRate; // Percentage
}
