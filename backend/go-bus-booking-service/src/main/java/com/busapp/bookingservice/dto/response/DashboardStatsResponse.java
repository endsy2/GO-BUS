package com.busapp.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private Long activeBookings;
    private BigDecimal totalRevenue;
    private Integer availableFleet;
    private Long pendingRefunds;
    private BigDecimal pendingRefundAmount;
    
    // Additional useful metrics
    private Long todayBookings;
    private BigDecimal todayRevenue;
    private Long pendingPayments;
    private Long confirmedBookings;
}
