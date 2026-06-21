package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.dto.response.*;
import com.busapp.bookingservice.model.enums.ReportPeriod;
import com.busapp.bookingservice.service.OperationReportService;
import com.busapp.bookingservice.util.DateRangeCalculator;
import com.busapp.bookingservice.util.DateRangeCalculator.DateRange;
import com.busapp.bookingservice.util.ExcelGeneratorUtil;
import com.busapp.bookingservice.util.ExcelResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports-operation")
@RequiredArgsConstructor
public class OperationReportController {

    private final OperationReportService operationReportService;

    @GetMapping("/bookings")
    public ResponseEntity<byte[]> getBookingReport(
            @RequestParam(defaultValue = "DAILY") ReportPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) throws IOException {

        DateRange dateRange = DateRangeCalculator.calculateDateRange(period, date, startDate, endDate, year, month);
        BookingReportResponse report = operationReportService.getBookingReport(dateRange.getStartDate(), dateRange.getEndDate());
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateBookingReportExcel(report),
                ExcelResponseUtil.generateBookingReportFilename(dateRange.getStartDate(), dateRange.getEndDate())
        );
    }

    @GetMapping("/revenue/routes")
    public ResponseEntity<byte[]> getRouteRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {

        List<RouteRevenueReportResponse> report = operationReportService.getRouteRevenueReport(startDate, endDate);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateRouteRevenueExcel(report),
                ExcelResponseUtil.generateRouteRevenueFilename(startDate, endDate)
        );
    }

    @GetMapping("/routes/popular")
    public ResponseEntity<byte[]> getPopularRoutesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") Integer limit) throws IOException {

        List<PopularRouteReportResponse> report = operationReportService.getPopularRoutesReport(startDate, endDate, limit);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generatePopularRoutesExcel(report),
                ExcelResponseUtil.generatePopularRoutesFilename(startDate, endDate)
        );
    }

    @GetMapping("/occupancy")
    public ResponseEntity<byte[]> getSeatOccupancyReport(
            @RequestParam(required = false) Long scheduleId) throws IOException {

        List<SeatOccupancyReportResponse> report = operationReportService.getSeatOccupancyReport(scheduleId);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateSeatOccupancyExcel(report),
                ExcelResponseUtil.generateSeatOccupancyFilename(scheduleId)
        );
    }

    @GetMapping("/tickets/sales")
    public ResponseEntity<byte[]> getTicketSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) Long busId) throws IOException {

        List<TicketSalesReportResponse> report = operationReportService.getTicketSalesReport(startDate, endDate, routeId, busId);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateTicketSalesExcel(report),
                ExcelResponseUtil.generateTicketSalesFilename(startDate, endDate)
        );
    }
}
