package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.dto.request.BakongRequest;
import com.busapp.bookingservice.dto.request.CheckTransactionRequest;
import com.busapp.bookingservice.dto.response.ApiResponse;
import com.busapp.bookingservice.dto.response.BakongResponse;
import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.service.impl.BakongService;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/payments/bakong/")
@RequiredArgsConstructor
public class BakongController {
    private final BakongService bakongService;


    @PostMapping("generateKHQR")
    public ResponseEntity<ApiResponse<BakongResponse>> generateKhqr(
            @Validated @RequestBody BakongRequest bakongRequest) {
        
        BakongResponse khqrResponse = bakongService.generateKhqr(bakongRequest);
        
        ApiResponse<BakongResponse> response = ApiResponse.of(
                HttpStatus.OK.value(),
                "KHQR generated successfully",
                khqrResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("checking-transaction")
    public ResponseEntity<ApiResponse<BakongResponse>> checkingTransaction(
            @Validated @RequestBody CheckTransactionRequest checkTransactionRequest, 
            @RequestParam("bookingId") Long bookingId) {
        
        BakongResponse bakongResponse = bakongService.checkingTransaction(bookingId, checkTransactionRequest);
        
        ApiResponse<BakongResponse> response = ApiResponse.of(
                HttpStatus.OK.value(),
                "Transaction check completed",
                bakongResponse);
        
        return ResponseEntity.ok(response);
    }
}
