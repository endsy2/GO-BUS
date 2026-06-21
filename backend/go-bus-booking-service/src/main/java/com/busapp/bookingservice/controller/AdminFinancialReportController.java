package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.dto.response.*;
import com.busapp.bookingservice.model.enums.ReportPeriod;
import com.busapp.bookingservice.service.FinancialReportService;
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
@RequestMapping("/api/admin/financial-reports")
@RequiredArgsConstructor
public class AdminFinancialReportController {

    private final FinancialReportService financialReportService;

    @GetMapping("/revenue")
    public ResponseEntity<byte[]> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") ReportPeriod period) throws IOException {
        
        List<RevenueReportResponse> report = financialReportService.getRevenueReport(startDate, endDate, period);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateRevenueReportExcel(report),
                ExcelResponseUtil.generateRevenueReportFilename(startDate, endDate, period.name())
        );
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<byte[]> getPaymentMethodReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        List<PaymentMethodReportResponse> report = financialReportService.getPaymentMethodReport(startDate, endDate);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generatePaymentMethodReportExcel(report),
                ExcelResponseUtil.generatePaymentMethodReportFilename(startDate, endDate)
        );
    }

    @GetMapping("/refunds-cancellations")
    public ResponseEntity<byte[]> getRefundCancellationReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") ReportPeriod period) throws IOException {
        
        List<RefundCancellationReportResponse> report = financialReportService.getRefundCancellationReport(startDate, endDate, period);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generateRefundCancellationReportExcel(report),
                ExcelResponseUtil.generateRefundCancellationReportFilename(startDate, endDate, period.name())
        );
    }

    @GetMapping("/promo-codes")
    public ResponseEntity<byte[]> getPromoCodeUsageReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
        List<PromoCodeUsageReportResponse> report = financialReportService.getPromoCodeUsageReport(startDate, endDate);
        
        return ExcelResponseUtil.createExcelResponse(
                ExcelGeneratorUtil.generatePromoCodeUsageReportExcel(report),
                ExcelResponseUtil.generatePromoCodeUsageReportFilename(startDate, endDate)
        );
    }
}
