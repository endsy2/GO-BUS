package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.dto.response.*;
import com.busapp.bookingservice.model.Booking;
import com.busapp.bookingservice.model.Payment;
import com.busapp.bookingservice.model.PromoCode;
import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.ReportPeriod;
import com.busapp.bookingservice.repository.BookingRepository;
import com.busapp.bookingservice.repository.PaymentRepository;
import com.busapp.bookingservice.repository.PromoCodeRepository;
import com.busapp.bookingservice.service.FinancialReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialReportServiceImpl implements FinancialReportService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PromoCodeRepository promoCodeRepository;

    @Override
    public List<RevenueReportResponse> getRevenueReport(LocalDate startDate, LocalDate endDate, ReportPeriod period) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<Booking> bookings = bookingRepository.findBookingsByDateRange(start, end);
        
        Map<String, List<Booking>> groupedBookings = groupBookingsByPeriod(bookings, period);
        
        return groupedBookings.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    String periodLabel = entry.getKey();
                    List<Booking> periodBookings = entry.getValue();
                    
                    long totalBookings = periodBookings.size();
                    long confirmedBookings = periodBookings.stream()
                            .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                            .count();
                    long cancelledBookings = periodBookings.stream()
                            .filter(b -> b.getBookingStatus() == BookingStatus.CANCELLED)
                            .count();
                    
                    double totalRevenue = periodBookings.stream()
                            .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount().doubleValue() : 0.0)
                            .sum();
                    
                    double confirmedRevenue = periodBookings.stream()
                            .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                            .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount().doubleValue() : 0.0)
                            .sum();
                    
                    double refundedAmount = periodBookings.stream()
                            .filter(b -> b.getBookingStatus() == BookingStatus.CANCELLED)
                            .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount().doubleValue() : 0.0)
                            .sum();
                    
                    double netRevenue = confirmedRevenue - refundedAmount;
                    
                    return RevenueReportResponse.builder()
                            .period(periodLabel)
                            .totalBookings(totalBookings)
                            .confirmedBookings(confirmedBookings)
                            .cancelledBookings(cancelledBookings)
                            .totalRevenue(Math.round(totalRevenue * 100.0) / 100.0)
                            .confirmedRevenue(Math.round(confirmedRevenue * 100.0) / 100.0)
                            .refundedAmount(Math.round(refundedAmount * 100.0) / 100.0)
                            .netRevenue(Math.round(netRevenue * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentMethodReportResponse> getPaymentMethodReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<Booking> bookings = bookingRepository.findBookingsByDateRange(start, end);
        
        Map<PaymentMethodType, List<Booking>> groupedByMethod = bookings.stream()
                .filter(b -> b.getPaymentMethod() != null)
                .collect(Collectors.groupingBy(Booking::getPaymentMethod));
        
        double totalAmount = bookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount().doubleValue() : 0.0)
                .sum();
        
        return groupedByMethod.entrySet().stream()
                .map(entry -> {
                    PaymentMethodType method = entry.getKey();
                    List<Booking> methodBookings = entry.getValue();
                    
                    long totalTransactions = methodBookings.size();
                    long successfulTransactions = methodBookings.stream()
                            .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                            .count();
                    long failedTransactions = methodBookings.stream()
                            .filter(b -> b.getBookingStatus() == BookingStatus.CANCELLED || 
                                        b.getBookingStatus() == BookingStatus.FAILED)
                            .count();
                    
                    double methodAmount = methodBookings.stream()
                            .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                            .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount().doubleValue() :  0.0)
                            .sum();
                    
                    double avgTransactionValue = successfulTransactions > 0 ? 
                            methodAmount / successfulTransactions : 0.0;
                    
                    double percentageOfTotal = totalAmount > 0 ? 
                            (methodAmount / totalAmount) * 100.0 : 0.0;
                    
                    return PaymentMethodReportResponse.builder()
                            .paymentMethod(method.name())
                            .totalTransactions(totalTransactions)
                            .successfulTransactions(successfulTransactions)
                            .failedTransactions(failedTransactions)
                            .totalAmount(Math.round(methodAmount * 100.0) / 100.0)
                            .averageTransactionValue(Math.round(avgTransactionValue * 100.0) / 100.0)
                            .percentageOfTotal(Math.round(percentageOfTotal * 100.0) / 100.0)
                            .build();
                })
                .sorted(Comparator.comparing(PaymentMethodReportResponse::getTotalAmount).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundCancellationReportResponse> getRefundCancellationReport(LocalDate startDate, LocalDate endDate, ReportPeriod period) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<Booking> allBookings = bookingRepository.findBookingsByDateRange(start, end);
        List<Booking> cancelledBookings = allBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CANCELLED)
                .collect(Collectors.toList());
        
        Map<String, List<Booking>> groupedCancellations = groupBookingsByPeriod(cancelledBookings, period);
        Map<String, List<Booking>> groupedAllBookings = groupBookingsByPeriod(allBookings, period);
        
        return groupedAllBookings.keySet().stream()
                .sorted()
                .map(periodLabel -> {
                    List<Booking> periodCancellations = groupedCancellations.getOrDefault(periodLabel, Collections.emptyList());
                    List<Booking> periodAllBookings = groupedAllBookings.get(periodLabel);
                    
                    long totalCancellations = periodCancellations.size();
                    double totalRefundAmount = periodCancellations.stream()
                            .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount().doubleValue() : 0.0)
                            .sum();
                    
                    double avgRefundAmount = totalCancellations > 0 ? 
                            totalRefundAmount / totalCancellations : 0.0;
                    
                    double cancellationRate = periodAllBookings.size() > 0 ? 
                            (totalCancellations * 100.0) / periodAllBookings.size() : 0.0;
                    
                    return RefundCancellationReportResponse.builder()
                            .period(periodLabel)
                            .totalCancellations(totalCancellations)
                            .totalRefundAmount(Math.round(totalRefundAmount * 100.0) / 100.0)
                            .averageRefundAmount(Math.round(avgRefundAmount * 100.0) / 100.0)
                            .userInitiatedCancellations(totalCancellations) // Can be enhanced with cancellation reason
                            .systemCancellations(0L) // Can be enhanced with cancellation reason
                            .cancellationRate(Math.round(cancellationRate * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PromoCodeUsageReportResponse> getPromoCodeUsageReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<PromoCode> promoCodes = promoCodeRepository.findAll();
        
        return promoCodes.stream()
                .map(promo -> {
                    List<Booking> promoBookings = promo.getBookings().stream()
                            .filter(b -> b.getCreatedAt().isAfter(start) && b.getCreatedAt().isBefore(end))
                            .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                            .collect(Collectors.toList());
                    
                    long totalUsages = promoBookings.size();
                    
                    double totalRevenueWithPromo = promoBookings.stream()
                            .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount().doubleValue() : 0.0)
                            .sum();
                    
                    // Calculate discount given based on discount type
                    double totalDiscountGiven = promoBookings.stream()
                            .mapToDouble(b -> calculateDiscount(b, promo))
                            .sum();
                    
                    return PromoCodeUsageReportResponse.builder()
                            .promoId(promo.getId())
                            .promoCode(promo.getCode())
                            .discountType(promo.getDiscountType().name())
                            .discountValue(promo.getDiscountValue())
                            .totalUsages(totalUsages)
                            .maxUses(promo.getMaxUses())
                            .totalDiscountGiven(Math.round(totalDiscountGiven * 100.0) / 100.0)
                            .totalRevenueWithPromo(Math.round(totalRevenueWithPromo * 100.0) / 100.0)
                            .status(promo.getStatus().name())
                            .validFrom(promo.getValidFrom() != null ? promo.getValidFrom().toString() : "")
                            .validTo(promo.getValidTo() != null ? promo.getValidTo().toString() : "")
                            .build();
                })
                .filter(report -> report.getTotalUsages() > 0)
                .sorted(Comparator.comparing(PromoCodeUsageReportResponse::getTotalUsages).reversed())
                .collect(Collectors.toList());
    }

    // Helper methods

    private Map<String, List<Booking>> groupBookingsByPeriod(List<Booking> bookings, ReportPeriod period) {
        DateTimeFormatter formatter;
        
        switch (period) {
            case DAILY:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                break;
            case WEEKLY:
                return bookings.stream()
                        .collect(Collectors.groupingBy(b -> {
                            LocalDate date = b.getCreatedAt().toLocalDate();
                            int weekOfYear = date.getDayOfYear() / 7 + 1;
                            return date.getYear() + "-W" + String.format("%02d", weekOfYear);
                        }));
            case MONTHLY:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                break;
            default:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }
        
        DateTimeFormatter finalFormatter = formatter;
        return bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getCreatedAt().format(finalFormatter)));
    }

    private double calculateDiscount(Booking booking, PromoCode promo) {
        if (booking.getTotalAmount() == null) return 0.0;
        
        // The booking.totalAmount is already the discounted price
        // We need to calculate the original price to find the discount
        double discountedAmount = booking.getTotalAmount().doubleValue();
        double originalAmount;
        
        switch (promo.getDiscountType()) {
            case PERCENTAGE:
                // If discountedAmount = originalAmount * (1 - discount/100)
                // Then originalAmount = discountedAmount / (1 - discount/100)
                originalAmount = discountedAmount / (1 - promo.getDiscountValue() / 100.0);
                return originalAmount - discountedAmount;
            case FIXED:
                // For fixed discount, the discount is simply the fixed value
                return promo.getDiscountValue();
            default:
                return 0.0;
        }
    }
}
