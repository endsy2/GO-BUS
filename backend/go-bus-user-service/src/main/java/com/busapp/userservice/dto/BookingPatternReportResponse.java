package com.busapp.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPatternReportResponse {
    private String period; // Day of week or hour
    private Long totalBookings;
    private Long uniqueCustomers;
    private Double totalRevenue;
    private Double averageBookingValue;
}
