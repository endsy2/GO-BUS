package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.dto.request.RefundApprovalRequest;
import com.busapp.bookingservice.dto.response.ApiResponse;
import com.busapp.bookingservice.dto.response.RefundResponse;
import com.busapp.bookingservice.dto.response.RefundStatisticsResponse;
import com.busapp.bookingservice.model.enums.RefundStatus;
import com.busapp.bookingservice.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundService refundService;

    /**
     * Get all refunds with optional status filter
     * GET /api/admin/refunds
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RefundResponse>>> getAllRefunds(
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(required = false,defaultValue = "0") int page,
            @RequestParam(required = false,defaultValue = "10") int size) {
        
        log.info("[ADMIN_GET_REFUNDS] Fetching refunds - status: {}, page: {}, size: {}", status, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RefundResponse> refunds = refundService.getAllRefunds(status, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.<Page<RefundResponse>>builder()
                .status(HttpStatus.OK.value())
                .endpoint("/api/admin/refunds")
                .message("Refunds retrieved successfully")
                .data(refunds)
                .build()
        );
    }

    /**
     * Get refunds by date range
     * GET /api/admin/refunds/date-range
     */
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<Page<RefundResponse>>> getRefundsByDateRange(
            @RequestParam RefundStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("[ADMIN_GET_REFUNDS_DATE_RANGE] status: {}, from: {}, to: {}", status, fromDate, toDate);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RefundResponse> refunds = refundService.getRefundsByDateRange(status, fromDate, toDate, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.<Page<RefundResponse>>builder()
                .status(HttpStatus.OK.value())
                .endpoint("/api/admin/refunds/date-range")
                .message("Refunds retrieved successfully")
                .data(refunds)
                .build()
        );
    }

    /**
     * Process a refund (approve or reject)
     * PUT /api/admin/refunds/{refundId}/process
     */
    @PutMapping("/{refundId}/process")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @PathVariable Long refundId,
            @Valid @RequestBody RefundApprovalRequest request,
            @RequestHeader("X-User-Id") Long adminId) {
        
        log.info("[ADMIN_PROCESS_REFUND] Admin {} processing refund {} - approved: {}", 
            adminId, refundId, request.getApproved());
        
        RefundResponse response = refundService.processRefund(refundId, request, adminId);
        
        return ResponseEntity.ok(
            ApiResponse.<RefundResponse>builder()
                .status(HttpStatus.OK.value())
                .endpoint("/api/admin/refunds/" + refundId + "/process")
                .message("Refund processed successfully")
                .data(response)
                .build()
        );
    }

    /**
     * Get refund statistics
     * GET /api/admin/refunds/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<RefundStatisticsResponse>> getRefundStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        
        log.info("[ADMIN_REFUND_STATS] Fetching statistics from {} to {}", fromDate, toDate);
        
        RefundStatisticsResponse stats = refundService.getRefundStatistics(fromDate, toDate);
        
        return ResponseEntity.ok(
            ApiResponse.<RefundStatisticsResponse>builder()
                .status(HttpStatus.OK.value())
                .endpoint("/api/admin/refunds/statistics")
                .message("Refund statistics retrieved successfully")
                .data(stats)
                .build()
        );
    }
}
