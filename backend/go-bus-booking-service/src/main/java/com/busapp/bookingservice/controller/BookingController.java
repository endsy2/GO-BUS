package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.dto.request.BookingFilterRequest;
import com.busapp.bookingservice.dto.request.RefundRequest;
import com.busapp.bookingservice.dto.response.ApiResponse;
import com.busapp.bookingservice.dto.response.BookingDetailResponse;
import com.busapp.bookingservice.dto.request.BookingRequest;
import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.dto.response.RefundResponse;
import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import com.busapp.bookingservice.security.CurrentUser;
import com.busapp.bookingservice.service.BookingService;
import com.busapp.bookingservice.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final RefundService refundService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> filterBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) BookingStatus bookingStatus,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) PaymentMethodType paymentMethod,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(value = "pageStart", defaultValue = "1") int pageStart,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        BookingFilterRequest filter = BookingFilterRequest.builder()
                .userId(userId)
                .scheduleId(scheduleId)
                .bookingStatus(bookingStatus)
                .paymentStatus(paymentStatus)
                .paymentMethod(paymentMethod)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .createdFrom(createdFrom)
                .createdTo(createdTo)
                .build();

        Page<BookingResponse> page = bookingService.filterBookings(filter, pageStart - 1, pageSize);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "Bookings retrieved successfully", page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDetailResponse>> getBookingById(@PathVariable Long id) {
        ApiResponse<BookingDetailResponse> response = ApiResponse.of(
                HttpStatus.OK.value(),
                "Booking retrieved successfully",
                bookingService.getBookingDetailById(id));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest bookingRequest,
            @CurrentUser Long userId) {
        log.debug("[BOOKING] createBooking called by userId={}", userId);
        ApiResponse<BookingResponse> response = ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Booking created successfully",
                bookingService.createBooking(bookingRequest, userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable Long id) {
        ApiResponse<BookingResponse> response = ApiResponse.of(
                HttpStatus.OK.value(),
                "Booking cancelled successfully",
                bookingService.cancelBooking(id));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        ApiResponse<Void> response = ApiResponse.of(HttpStatus.OK.value(), 
                "Booking deleted successfully", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/refund")
    public ResponseEntity<ApiResponse<RefundResponse>> requestRefund(
            @PathVariable Long bookingId,
            @Valid @RequestBody RefundRequest request,
            @CurrentUser Long userId) {
        ApiResponse<RefundResponse> response = ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Refund request submitted successfully",
                refundService.requestRefund(bookingId, request, userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
