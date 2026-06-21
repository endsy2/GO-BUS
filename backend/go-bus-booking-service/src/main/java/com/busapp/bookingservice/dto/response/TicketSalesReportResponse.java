package com.busapp.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketSalesReportResponse {
    private Long scheduleId;
    private Long routeId;
    private Long busId;
    private Long totalTicketsSold;
    private Double totalRevenue;
}
