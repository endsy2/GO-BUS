package com.busapp.busservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutePerformanceReportResponse {
    private Long routeId;
    private String origin;
    private String destination;
    private Double distanceKm;
    private Integer totalBuses;
    private Integer totalSchedules;
    private Long totalBookings;
    private Double totalRevenue;
    private Double averageBookingsPerSchedule;
}
