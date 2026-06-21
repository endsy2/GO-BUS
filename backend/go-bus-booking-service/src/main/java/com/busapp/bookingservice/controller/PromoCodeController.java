package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.dto.response.ApiResponse;
import com.busapp.bookingservice.dto.request.PromoCodeRequest;
import com.busapp.bookingservice.dto.response.PromoCodeResponse;
import com.busapp.bookingservice.service.PromoCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promos")
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PromoCodeResponse>>> getAllPromos() {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "Promo codes retrieved successfully",
                promoCodeService.getAllPromos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> getPromoById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "Promo code retrieved successfully",
                promoCodeService.getPromoById(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> getPromoByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "Promo code retrieved successfully",
                promoCodeService.getPromoByCode(code)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PromoCodeResponse>> createPromo(
            @Valid @RequestBody PromoCodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED.value(), 
                        "Promo code created successfully",
                        promoCodeService.createPromo(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> updatePromo(
            @PathVariable Long id,
            @Valid @RequestBody PromoCodeRequest request) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "Promo code updated successfully",
                promoCodeService.updatePromo(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePromo(@PathVariable Long id) {
        promoCodeService.deletePromo(id);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), 
                "Promo code deleted successfully", null));
    }
}
