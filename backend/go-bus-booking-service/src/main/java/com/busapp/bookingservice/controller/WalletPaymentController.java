package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.dto.request.WalletPaymentBookingRequest;
import com.busapp.bookingservice.dto.response.ApiResponse;
import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.service.WalletPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class WalletPaymentController {
    private final WalletPaymentService  walletPaymentService;

    @PostMapping("/{bookingId}/wallet")
    public ResponseEntity<ApiResponse<BookingResponse>> walletPayment(
            @PathVariable("bookingId") Long bookingId,
            @RequestHeader("X-Wallet-Session") String walletSessionToken,
            @RequestBody WalletPaymentBookingRequest request) {
        return ResponseEntity.ok().body(
                ApiResponse.of(
                        HttpStatus.OK.value(),
                        "successfully",
                        walletPaymentService.walletBookingPayment(bookingId, walletSessionToken, request)
                )
        );
    }
}
