package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.dto.response.ApiResponse;
import com.busapp.bookingservice.dto.response.PaymentResponse;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentsByBooking(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "Payments retrieved successfully",
                paymentService.getPaymentsByBooking(bookingId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id,@RequestParam PaymentMethodType paymentMethodId ) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "Payment retrieved successfully",
                paymentService.getPaymentById(id,paymentMethodId)));
    }

}
