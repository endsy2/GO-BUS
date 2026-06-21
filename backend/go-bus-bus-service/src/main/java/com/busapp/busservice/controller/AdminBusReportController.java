package com.busapp.busservice.controller;

import com.busapp.busservice.dto.*;
import com.busapp.busservice.service.BusReportService;
import com.busapp.busservice.util.ExcelGeneratorUtil;
import com.busapp.busservice.util.ExcelResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports-bus")
@RequiredArgsConstructor
public class AdminBusReportController {

    private final BusReportService busReportService;

    @GetMapping("/buses/utilization")
    public ResponseEntity<byte[]> getBusUtilizationReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        List<BusUtilizationReportResponse> report = busReportService.getBusUtilizationReport(startDate, endDate);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateBusUtilizationExcel(report),
                ExcelResponseUtil.generateBusUtilizationFilename(startDate, endDate)
        );
    }

    @GetMapping("/routes/performance")
    public ResponseEntity<byte[]> getRoutePerformanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        List<RoutePerformanceReportResponse> report = busReportService.getRoutePerformanceReport(startDate, endDate);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateRoutePerformanceExcel(report),
                ExcelResponseUtil.generateRoutePerformanceFilename(startDate, endDate)
        );
    }

    @GetMapping("/buses/inactive")
    public ResponseEntity<byte[]> getInactiveBusReport(
            @RequestParam(defaultValue = "30") Integer daysThreshold) throws IOException {
        
        List<InactiveBusReportResponse> report = busReportService.getInactiveBusReport(daysThreshold);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateInactiveBusExcel(report),
                ExcelResponseUtil.generateInactiveBusFilename(daysThreshold)
        );
    }

    @GetMapping("/routes/inactive")
    public ResponseEntity<byte[]> getInactiveRouteReport(
            @RequestParam(defaultValue = "30") Integer daysThreshold) throws IOException {
        
        List<InactiveRouteReportResponse> report = busReportService.getInactiveRouteReport(daysThreshold);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateInactiveRouteExcel(report),
                ExcelResponseUtil.generateInactiveRouteFilename(daysThreshold)
        );
    }

    @GetMapping("/buses/capacity-analysis")
    public ResponseEntity<byte[]> getBusCapacityAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        List<BusCapacityAnalysisResponse> report = busReportService.getBusCapacityAnalysis(startDate, endDate);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateBusCapacityAnalysisExcel(report),
                ExcelResponseUtil.generateBusCapacityAnalysisFilename(startDate, endDate)
        );
    }
}
