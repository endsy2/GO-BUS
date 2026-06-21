package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.dto.request.RefundApprovalRequest;
import com.busapp.bookingservice.dto.response.ApiResponse;
import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.dto.request.AdminBookingFilterRequest;
import com.busapp.bookingservice.dto.response.RefundResponse;
import com.busapp.bookingservice.dto.response.UserStatusResponse;
import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import com.busapp.bookingservice.security.CurrentUser;
import org.springframework.format.annotation.DateTimeFormat;
import com.busapp.bookingservice.service.AdminBookingService;
import com.busapp.bookingservice.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Admin endpoints for booking management.
 * All routes require ADMIN_ACCESS permission (enforced by RouteAuthorizationFilter).
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class  AdminBookingController {

    private final AdminBookingService adminBookingService;
    private final RefundService refundService;

    // ── List / Filter ─────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getBookings(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) BookingStatus bookingStatus,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) PaymentMethodType paymentMethod,
            @RequestParam(required = false) Boolean refund,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(value = "pageStart", defaultValue = "0") int pageStart,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        AdminBookingFilterRequest filter = AdminBookingFilterRequest.builder()
                .username(username)
                .scheduleId(scheduleId)
                .bookingStatus(bookingStatus)
                .paymentStatus(paymentStatus)
                .paymentMethod(paymentMethod)
                .refund(refund)
                .departureFrom(departureFrom)
                .departureTo(departureTo)
                .createdFrom(createdFrom)
                .createdTo(createdTo)
                .page(pageStart)
                .size(pageSize)
                .build();

        Page<BookingResponse> page = adminBookingService.getBookings(filter);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "Bookings retrieved successfully", page));
    }
    /**
     * Summary booking stats for a single user (totalBookings, totalSpent, activeTickets).
     * Used by user-service admin panel. Always returns a map — never null, never empty —
     * so the caller never gets a deserialization error even for brand-new users.
     */
    @GetMapping("/user-detail-stats/{userId}")
    public ResponseEntity<ApiResponse<UserStatusResponse>> getUserDetailStats(@PathVariable("userId") Long userId) {
     return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(),
             "Successfully",
             adminBookingService.getUserLifetimeStats(userId)));
    }

    // ── Admin Actions ─────────────────────────────────────────────────────────


    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "Booking cancelled",
                adminBookingService.cancelBooking(id)));
    }

    @PatchMapping("/{id}/force-pay")
    public ResponseEntity<ApiResponse<BookingResponse>> forceMarkPaid(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "Booking marked as paid",
                adminBookingService.forceMarkPaid(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Long id) {
        adminBookingService.deleteBooking(id);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "Booking deleted successfully", null));
    }

    @PatchMapping("/refunds/{refundId}/process")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @PathVariable Long refundId,
            @Valid @RequestBody RefundApprovalRequest request,
            @CurrentUser Long adminId) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(),
                request.getApproved() ? "Refund approved successfully" : "Refund rejected",
                refundService.processRefund(refundId, request, adminId)));
    }
}
