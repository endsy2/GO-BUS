package com.busapp.userservice.controller;

import com.busapp.userservice.dto.*;
import com.busapp.userservice.service.CustomerReportService;
import com.busapp.userservice.util.ExcelGeneratorUtil;
import com.busapp.userservice.util.ExcelResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports-customer")
@RequiredArgsConstructor
public class AdminCustomerReportController {

    private final CustomerReportService customerReportService;

    @GetMapping("/customers/active")
    public ResponseEntity<byte[]> getActiveCustomerReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        List<ActiveCustomerReportResponse> report = customerReportService.getActiveCustomerReport(startDate, endDate);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateActiveCustomerExcel(report),
                ExcelResponseUtil.generateActiveCustomerFilename(startDate, endDate)
        );
    }

    @GetMapping("/customers/frequent-travelers")
    public ResponseEntity<byte[]> getFrequentTravelerReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "50") Integer limit) throws IOException {
        
        List<FrequentTravelerReportResponse> report = customerReportService.getFrequentTravelerReport(startDate, endDate, limit);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateFrequentTravelerExcel(report),
                ExcelResponseUtil.generateFrequentTravelerFilename(startDate, endDate)
        );
    }

    @GetMapping("/customers/demographics")
    public ResponseEntity<byte[]> getCustomerDemographicsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        List<CustomerDemographicsReportResponse> report = customerReportService.getCustomerDemographicsReport(startDate, endDate);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateCustomerDemographicsExcel(report),
                ExcelResponseUtil.generateCustomerDemographicsFilename(startDate, endDate)
        );
    }

    @GetMapping("/bookings/patterns-by-day")
    public ResponseEntity<byte[]> getBookingPatternsByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        List<BookingPatternReportResponse> report = customerReportService.getBookingPatternsByDay(startDate, endDate);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateBookingPatternsExcel(report, "Booking Patterns by Day of Week"),
                ExcelResponseUtil.generateBookingPatternsByDayFilename(startDate, endDate)
        );
    }

    @GetMapping("/bookings/patterns-by-hour")
    public ResponseEntity<byte[]> getBookingPatternsByHour(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        List<BookingPatternReportResponse> report = customerReportService.getBookingPatternsByHour(startDate, endDate);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateBookingPatternsExcel(report, "Booking Patterns by Hour"),
                ExcelResponseUtil.generateBookingPatternsByHourFilename(startDate, endDate)
        );
    }
}
