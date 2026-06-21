package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.response.*;

import java.time.LocalDate;
import java.util.List;

public interface OperationReportService {
    BookingReportResponse getBookingReport(LocalDate startDate, LocalDate endDate);
    List<RouteRevenueReportResponse> getRouteRevenueReport(LocalDate startDate, LocalDate endDate);
    List<PopularRouteReportResponse> getPopularRoutesReport(LocalDate startDate, LocalDate endDate, Integer limit);
    List<SeatOccupancyReportResponse> getSeatOccupancyReport(Long scheduleId);
    List<TicketSalesReportResponse> getTicketSalesReport(LocalDate startDate, LocalDate endDate, Long routeId, Long busId);
}
