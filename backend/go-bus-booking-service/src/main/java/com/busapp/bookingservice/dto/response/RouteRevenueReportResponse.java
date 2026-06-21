package com.busapp.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRevenueReportResponse {
    private Long routeId;
    private String routeName;
    private Long totalBookings;
    private Long confirmedBookings;
    private Long cancelledBookings;
    private Double totalRevenue;
    private Long totalTicketsSold;
}
