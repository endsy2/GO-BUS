package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.client.BusClient;
import com.busapp.bookingservice.dto.event.DashboardUpdateEvent;
import com.busapp.bookingservice.dto.response.BookingVelocityResponse;
import com.busapp.bookingservice.dto.response.DashboardStatsResponse;
import com.busapp.bookingservice.dto.response.RevenueStreamResponse;
import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import com.busapp.bookingservice.model.enums.RefundStatus;
import com.busapp.bookingservice.repository.BookingRepository;
import com.busapp.bookingservice.repository.PaymentRepository;
import com.busapp.bookingservice.repository.RefundRepository;
import com.busapp.bookingservice.service.DashboardService;
import com.busapp.bookingservice.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final BookingRepository bookingRepository;
    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final BusClient busClient;
    private final WebSocketService webSocketService;

    @Override
    public DashboardStatsResponse getDashboardStats(LocalDateTime fromDate, LocalDateTime toDate) {
        log.info("Fetching dashboard statistics from {} to {}", fromDate, toDate);

        // Set default date range if not provided (last 30 days)
        if (fromDate == null) {
            fromDate = LocalDateTime.now().minusDays(30);
        }
        if (toDate == null) {
            toDate = LocalDateTime.now();
        }

        // 1. Active Bookings (CONFIRMED or PENDING status, not deleted)
        Long activeBookings = bookingRepository.countByBookingStatusInAndIsDeleted(
                java.util.List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING),
                false
        );

        // 2. Total Revenue (sum of all CONFIRMED bookings with SUCCESS payment)
        BigDecimal totalRevenue = bookingRepository.sumTotalAmountByBookingStatusAndPaymentStatusAndCreatedAtBetween(
                BookingStatus.CONFIRMED,
                PaymentStatus.SUCCESS,
                fromDate,
                toDate
        );
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        // 3. Available Fleet (active buses from bus-service)
        Integer availableFleet = 0;
        try {
            var busResponse = busClient.getActiveBusCount();
            if (busResponse != null && busResponse.getBody() != null) {
                availableFleet = busResponse.getBody().data();
            }
        } catch (Exception e) {
            log.error("Failed to fetch available fleet count from bus-service", e);
        }

        // 4. Pending Refunds (count and total amount)
        Long pendingRefunds = refundRepository.countByStatus(RefundStatus.PENDING);
        
        BigDecimal pendingRefundAmount = refundRepository.getTotalRefundAmountByStatus(RefundStatus.PENDING);
        if (pendingRefundAmount == null) {
            pendingRefundAmount = BigDecimal.ZERO;
        }

        // Additional metrics
        // Today's bookings
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        Long todayBookings = bookingRepository.countByCreatedAtBetweenAndIsDeleted(
                todayStart,
                todayEnd,
                false
        );

        // Today's revenue
        BigDecimal todayRevenue = bookingRepository.sumTotalAmountByBookingStatusAndPaymentStatusAndCreatedAtBetween(
                BookingStatus.CONFIRMED,
                PaymentStatus.SUCCESS,
                todayStart,
                todayEnd
        );
        if (todayRevenue == null) {
            todayRevenue = BigDecimal.ZERO;
        }

        // Pending payments
        Long pendingPayments = bookingRepository.countByPaymentStatusAndIsDeleted(
                PaymentStatus.PENDING,
                false
        );

        // Confirmed bookings
        Long confirmedBookings = bookingRepository.countByBookingStatusAndIsDeleted(
                BookingStatus.CONFIRMED,
                false
        );

        return DashboardStatsResponse.builder()
                .activeBookings(activeBookings)
                .totalRevenue(totalRevenue)
                .availableFleet(availableFleet)
                .pendingRefunds(pendingRefunds)
                .pendingRefundAmount(pendingRefundAmount)
                .todayBookings(todayBookings)
                .todayRevenue(todayRevenue)
                .pendingPayments(pendingPayments)
                .confirmedBookings(confirmedBookings)
                .build();
    }
    
    /**
     * Broadcast dashboard stats update via WebSocket
     */
    public void broadcastDashboardStats() {
        try {
            DashboardStatsResponse stats = getDashboardStats(null, null);
            
            DashboardUpdateEvent event = DashboardUpdateEvent.builder()
                    .type("DASHBOARD_UPDATE")
                    .activeBookings(stats.getActiveBookings())
                    .todayRevenue(stats.getTodayRevenue())
                    .pendingRefunds(stats.getPendingRefunds())
                    .todayBookings(stats.getTodayBookings())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            webSocketService.broadcastDashboardUpdate(event);
        } catch (Exception e) {
            log.error("Failed to broadcast dashboard stats", e);
        }
    }
    
    @Override
    public List<BookingVelocityResponse> getBookingVelocityTrend(LocalDateTime fromDate, LocalDateTime toDate) {
        log.info("Fetching booking velocity trend from {} to {}", fromDate, toDate);

        // Set default date range if not provided (last 30 days)
        if (fromDate == null) {
            fromDate = LocalDateTime.now().minusDays(30).with(LocalTime.MIN);
        }
        if (toDate == null) {
            toDate = LocalDateTime.now().with(LocalTime.MAX);
        }

        // Step 1: Get booking data from booking-service (own database)
        List<Object[]> rawData = bookingRepository.getBookingVelocityData(fromDate, toDate);
        
        if (rawData.isEmpty()) {
            log.info("No booking data found for the specified date range");
            return new ArrayList<>();
        }

        // Step 2: Collect all unique seat IDs
        Set<Long> seatIds = rawData.stream()
                .map(row -> ((Number) row[1]).longValue())
                .collect(Collectors.toSet());

        // Step 3: Fetch seat types from bus-service via Feign (batch call)
        Map<Long, String> seatTypeMap = new HashMap<>();
        try {
            var response = busClient.getSeatTypesBatch(seatIds);
            if (response != null && response.getBody() != null && response.getBody().data() != null) {
                seatTypeMap = response.getBody().data();
            }
        } catch (Exception e) {
            log.error("Failed to fetch seat types from bus-service", e);
            // Continue with empty map - will default to UNKNOWN
        }

        // Step 4: Group data by date and seat type
        Map<LocalDate, Map<String, VelocityData>> groupedData = new LinkedHashMap<>();
        
        for (Object[] row : rawData) {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            LocalDate date = sqlDate.toLocalDate();
            Long seatId = ((Number) row[1]).longValue();
            BigDecimal amount = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
            
            // Get seat type from the map (fetched via Feign)
            String seatType = seatTypeMap.getOrDefault(seatId, "NORMAL"); // Default to NORMAL if not found

            groupedData
                    .computeIfAbsent(date, k -> new HashMap<>())
                    .compute(seatType, (key, existing) -> {
                        if (existing == null) {
                            return new VelocityData(1L, amount);
                        } else {
                            return new VelocityData(
                                    existing.count + 1,
                                    existing.revenue.add(amount)
                            );
                        }
                    });
        }

        // Step 5: Convert to response format
        List<BookingVelocityResponse> responses = new ArrayList<>();

        for (Map.Entry<LocalDate, Map<String, VelocityData>> entry : groupedData.entrySet()) {
            LocalDate date = entry.getKey();
            Map<String, VelocityData> seatTypeData = entry.getValue();

            VelocityData seaterData = seatTypeData.getOrDefault("NORMAL", new VelocityData(0L, BigDecimal.ZERO));
            VelocityData vipData = seatTypeData.getOrDefault("VIP", new VelocityData(0L, BigDecimal.ZERO));
            VelocityData sleeperData = seatTypeData.getOrDefault("SLEEPER", new VelocityData(0L, BigDecimal.ZERO));

            Long totalBookings = seaterData.count + vipData.count + sleeperData.count;

            // Calculate percentages
            Double seaterPercentage = totalBookings > 0
                    ? (seaterData.count * 100.0) / totalBookings
                    : 0.0;
            Double sleeperPercentage = totalBookings > 0 
                    ? (sleeperData.count * 100.0) / totalBookings 
                    : 0.0;

            BookingVelocityResponse response = BookingVelocityResponse.builder()
                    .date(date)
                    .seaterBookings(seaterData.count)
                    .sleeperBookings(sleeperData.count)
                    .seaterRevenue(seaterData.revenue)
                    .sleeperRevenue(sleeperData.revenue)
                    .seaterPercentage(Math.round(seaterPercentage * 100.0) / 100.0)
                    .sleeperPercentage(Math.round(sleeperPercentage * 100.0) / 100.0)
                    .build();

            responses.add(response);
        }

        log.info("Retrieved {} days of booking velocity data", responses.size());
        return responses;
    }
    
    @Override
    public RevenueStreamResponse getRevenueStream(LocalDateTime fromDate, LocalDateTime toDate) {
        log.info("Fetching revenue stream by payment method from {} to {}", fromDate, toDate);

        // Set default date range if not provided (last 7 days for weekly view)
        if (fromDate == null) {
            fromDate = LocalDateTime.now().minusDays(7).with(LocalTime.MIN);
        }
        if (toDate == null) {
            toDate = LocalDateTime.now().with(LocalTime.MAX);
        }

        // Get revenue data grouped by payment method
        List<Object[]> rawData = paymentRepository.getRevenueByPaymentMethod(fromDate, toDate);
        
        if (rawData.isEmpty()) {
            log.info("No payment data found for the specified date range");
            return RevenueStreamResponse.builder()
                    .weeklyTotal(BigDecimal.ZERO)
                    .paymentMethods(new ArrayList<>())
                    .build();
        }

        // Calculate total revenue
        BigDecimal totalRevenue = rawData.stream()
                .map(row -> new BigDecimal(row[1].toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build payment method breakdown
        List<RevenueStreamResponse.PaymentMethodRevenue> paymentMethods = rawData.stream()
                .map(row -> {
                    PaymentMethodType method = (PaymentMethodType) row[0];
                    BigDecimal amount = new BigDecimal(row[1].toString());
                    Long transactionCount = ((Number) row[2]).longValue();
                    
                    // Calculate percentage
                    Double percentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                            ? amount.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                                    .multiply(new BigDecimal("100"))
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .doubleValue()
                            : 0.0;

                    return RevenueStreamResponse.PaymentMethodRevenue.builder()
                            .method(method.name().isEmpty() ?" ":method.name())
                            .amount(amount.setScale(2, RoundingMode.HALF_UP))
                            .percentage(percentage)
                            .transactionCount(transactionCount)
                            .build();
                })
                .collect(Collectors.toList());

        log.info("Retrieved revenue stream for {} payment methods, total: {}", 
                paymentMethods.size(), totalRevenue);

        return RevenueStreamResponse.builder()
                .weeklyTotal(totalRevenue.setScale(2, RoundingMode.HALF_UP))
                .paymentMethods(paymentMethods)
                .build();
    }

    // Helper class to hold velocity data
    private static class VelocityData {
        Long count;
        BigDecimal revenue;

        VelocityData(Long count, BigDecimal revenue) {
            this.count = count;
            this.revenue = revenue;
        }
    }
}
