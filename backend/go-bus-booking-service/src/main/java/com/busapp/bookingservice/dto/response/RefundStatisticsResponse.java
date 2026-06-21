package com.busapp.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundStatisticsResponse {
    private Long totalPending;
    private Long totalApproved;
    private Long totalRejected;
    private Double totalPendingAmount;
    private Double totalApprovedAmount;
    private Double totalRejectedAmount;
    private Double averageRefundAmount;
}
