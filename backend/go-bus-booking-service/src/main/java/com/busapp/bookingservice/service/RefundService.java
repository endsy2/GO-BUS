package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.request.RefundApprovalRequest;
import com.busapp.bookingservice.dto.request.RefundRequest;
import com.busapp.bookingservice.dto.response.RefundDetailResponse;
import com.busapp.bookingservice.dto.response.RefundResponse;
import com.busapp.bookingservice.dto.response.RefundStatisticsResponse;
import com.busapp.bookingservice.model.enums.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface RefundService {
    
    // ── User Operations ───────────────────────────────────────────────────────
    
    /**
     * User requests a refund for their booking
     */
    RefundResponse requestRefund(Long bookingId, RefundRequest request, Long userId);
    
    /**
     * Get all refunds for a specific user with full booking, schedule, and route detail
     */
    Page<RefundDetailResponse> getMyRefunds(Long userId, RefundStatus status, Pageable pageable);
    
    /**
     * Get a specific refund by ID (user must own the booking)
     */
    RefundResponse getRefundById(Long refundId, Long userId);
    
    /**
     * Get detailed refund information with booking, schedule, route, and wallet data
     */
    RefundDetailResponse getRefundDetailById(Long refundId, Long userId);
    
    // ── Admin Operations ──────────────────────────────────────────────────────
    
    /**
     * Admin processes a refund (approve or reject)
     */
    RefundResponse processRefund(Long refundId, RefundApprovalRequest request, Long adminId);
    
    /**
     * Get all refunds with optional status filter
     */
    Page<RefundResponse> getAllRefunds(RefundStatus status, Pageable pageable);
    
    /**
     * Get refunds within a date range and status
     */
    Page<RefundResponse> getRefundsByDateRange(
            RefundStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    );
    
    /**
     * Get refund statistics for admin dashboard
     */
    RefundStatisticsResponse getRefundStatistics(LocalDateTime fromDate, LocalDateTime toDate);
}
