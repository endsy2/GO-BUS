package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.response.DashboardStatsResponse;
import com.busapp.bookingservice.dto.response.RevenueStreamResponse;

import java.time.LocalDateTime;

public interface DashboardService {
    /**
     * Get dashboard statistics including active bookings, revenue, fleet, and refunds
     * 
     * @param fromDate Optional start date for filtering
     * @param toDate Optional end date for filtering
     * @return Dashboard statistics
     */
    DashboardStatsResponse getDashboardStats(LocalDateTime fromDate, LocalDateTime toDate);
    
    /**
     * Get booking velocity trends grouped by date and seat type
     * This shows day-by-day booking patterns to measure business growth
     * 
     * @param fromDate Start date for analysis
     * @param toDate End date for analysis
     * @return List of daily booking statistics by seat type
     */
    java.util.List<com.busapp.bookingservice.dto.response.BookingVelocityResponse> getBookingVelocityTrend(
            LocalDateTime fromDate, 
            LocalDateTime toDate);
    
    /**
     * Get revenue stream breakdown by payment method
     * Shows weekly revenue split across different payment methods (Bakong, Card, Wallet, etc.)
     * 
     * @param fromDate Start date for analysis
     * @param toDate End date for analysis
     * @return Revenue breakdown by payment method with percentages
     */
    RevenueStreamResponse getRevenueStream(LocalDateTime fromDate, LocalDateTime toDate);
}
