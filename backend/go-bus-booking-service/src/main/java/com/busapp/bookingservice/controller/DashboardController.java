package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.dto.response.ApiResponse;
import com.busapp.bookingservice.dto.response.DashboardStatsResponse;
import com.busapp.bookingservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Dashboard controller for admin statistics and metrics
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get dashboard statistics including:
     * - Active bookings (CONFIRMED + PENDING)
     * - Total revenue (CONFIRMED bookings with SUCCESS payment)
     * - Available fleet (active buses)
     * - Pending refunds (count and amount)
     * - Today's bookings and revenue
     * - Pending payments
     * - Confirmed bookings
     * 
     * @param fromDate Optional start date for filtering (default: 30 days ago)
     * @param toDate Optional end date for filtering (default: now)
     * @return Dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime fromDate,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime toDate) {
        
        DashboardStatsResponse stats = dashboardService.getDashboardStats(fromDate, toDate);
        
        return ResponseEntity.ok(
                ApiResponse.of(
                        HttpStatus.OK.value(),
                        "Dashboard statistics retrieved successfully",
                        stats
                )
        );
    }
    
    /**
     * Get booking velocity trend analysis
     * Shows day-by-day booking patterns broken down by seat type (NORMAL, VIP, SLEEPER)
     * 
     * This endpoint measures business velocity and helps admins:
     * - Track if business is growing or slowing down
     * - Identify which seat types are most popular
     * - Make inventory decisions (e.g., upgrade more buses to normal if sleeper is underutilized)
     * - Spot trends and patterns in customer preferences
     * 
     * @param fromDate Optional start date (default: 30 days ago)
     * @param toDate Optional end date (default: now)
     * @return List of daily booking statistics by seat type
     */
    @GetMapping("/velocity")
    public ResponseEntity<ApiResponse<java.util.List<com.busapp.bookingservice.dto.response.BookingVelocityResponse>>> getBookingVelocity(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime fromDate,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime toDate) {
        
        java.util.List<com.busapp.bookingservice.dto.response.BookingVelocityResponse> velocity = 
                dashboardService.getBookingVelocityTrend(fromDate, toDate);
        
        return ResponseEntity.ok(
                ApiResponse.of(
                        HttpStatus.OK.value(),
                        "Booking velocity trend retrieved successfully",
                        velocity
                )
        );
    }
    
    /**
     * Get revenue stream breakdown by payment method
     * Shows weekly revenue split across different payment methods (Bakong, Card, Wallet, etc.)
     * 
     * This endpoint helps admins:
     * - Understand which payment methods customers prefer
     * - Identify revenue distribution across payment channels
     * - Make decisions about payment method priorities and integrations
     * - Track transaction volumes per payment method
     * 
     * @param fromDate Optional start date (default: 7 days ago for weekly view)
     * @param toDate Optional end date (default: now)
     * @return Revenue breakdown by payment method with percentages and transaction counts
     */
    @GetMapping("/revenue-stream")
    public ResponseEntity<ApiResponse<com.busapp.bookingservice.dto.response.RevenueStreamResponse>> getRevenueStream(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime fromDate,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime toDate) {
        
        com.busapp.bookingservice.dto.response.RevenueStreamResponse revenueStream = 
                dashboardService.getRevenueStream(fromDate, toDate);
        
        return ResponseEntity.ok(
                ApiResponse.of(
                        HttpStatus.OK.value(),
                        "Revenue stream retrieved successfully",
                        revenueStream
                )
        );
    }
}
