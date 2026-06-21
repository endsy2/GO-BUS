package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.dto.request.RefundRequest;
import com.busapp.bookingservice.dto.response.ApiResponse;
import com.busapp.bookingservice.dto.response.RefundDetailResponse;
import com.busapp.bookingservice.dto.response.RefundResponse;
import com.busapp.bookingservice.model.enums.RefundStatus;
import com.busapp.bookingservice.service.RefundService;
import com.busapp.bookingservice.util.UserUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final UserUtil userUtil;

    /**
     * Request a refund for a booking
     * POST /api/refunds/request/{bookingId}
     */
    @PostMapping("/request/{bookingId}")
    public ResponseEntity<ApiResponse<RefundResponse>> requestRefund(
            @PathVariable Long bookingId,
            @Valid @RequestBody RefundRequest request) {

        Long userId=userUtil.getCurrentUserId();
        
        RefundResponse response = refundService.requestRefund(bookingId, request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.<RefundResponse>builder()
                .status(HttpStatus.CREATED.value())
                .endpoint("/api/refunds/request/" + bookingId)
                .message("Refund request submitted successfully")
                .data(response)
                .build()
        );
    }

    /**
     * Get all my refunds
     * GET /api/refunds/my
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<RefundDetailResponse>>> getMyRefunds(
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = userUtil.getCurrentUserId();

        log.info("[GET_MY_REFUNDS] User {} fetching refunds - status: {}, page: {}, size: {}", userId, status, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RefundDetailResponse> refunds = refundService.getMyRefunds(userId, status, pageable);

        return ResponseEntity.ok(
            ApiResponse.<Page<RefundDetailResponse>>builder()
                .status(HttpStatus.OK.value())
                .endpoint("/api/refunds/my")
                .message("Refunds retrieved successfully")
                .data(refunds)
                .build()
        );
    }

    /**
     * Get a specific refund by ID with detailed information
     * GET /api/refunds/{refundId}
     */
    @GetMapping("/{refundId}")
    public ResponseEntity<ApiResponse<RefundDetailResponse>> getRefundById(
            @PathVariable Long refundId) {

        Long userId=userUtil.getCurrentUserId();
        log.info("[GET_REFUND_DETAIL] User {} fetching detailed refund {}", userId, refundId);
        
        RefundDetailResponse response = refundService.getRefundDetailById(refundId, userId);
        
        return ResponseEntity.ok(
            ApiResponse.<RefundDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .endpoint("/api/refunds/" + refundId)
                .message("Refund details retrieved successfully")
                .data(response)
                .build()
        );
    }
}
