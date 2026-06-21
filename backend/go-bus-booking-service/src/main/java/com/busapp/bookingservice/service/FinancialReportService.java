package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.response.*;
import com.busapp.bookingservice.model.enums.ReportPeriod;

import java.time.LocalDate;
import java.util.List;

public interface FinancialReportService {
    
    List<RevenueReportResponse> getRevenueReport(LocalDate startDate, LocalDate endDate, ReportPeriod period);
    
    List<PaymentMethodReportResponse> getPaymentMethodReport(LocalDate startDate, LocalDate endDate);
    
    List<RefundCancellationReportResponse> getRefundCancellationReport(LocalDate startDate, LocalDate endDate, ReportPeriod period);
    
    List<PromoCodeUsageReportResponse> getPromoCodeUsageReport(LocalDate startDate, LocalDate endDate);
}
