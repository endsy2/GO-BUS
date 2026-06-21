package com.busapp.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {
    private String period; // Date or period label
    private Long totalBookings;
    private Long confirmedBookings;
    private Long cancelledBookings;
    private Double totalRevenue;
    private Double confirmedRevenue;
    private Double refundedAmount;
    private Double netRevenue;
}
