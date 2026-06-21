package com.busapp.userservice.controller;

import com.busapp.userservice.dto.request.AuthRequest;
import com.busapp.userservice.dto.request.RefreshTokenRequest;
import com.busapp.userservice.dto.request.UserRequest;
import com.busapp.userservice.dto.response.ApiResponse;
import com.busapp.userservice.dto.response.AuthResponse;
import com.busapp.userservice.dto.response.RefreshTokenResponse;
import com.busapp.userservice.dto.response.RegisterResponse;
import com.busapp.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Body: { "userName": "...", "fullName": "...", "email": "...", "password": "..." }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody UserRequest request) {
        ApiResponse<RegisterResponse> response = ApiResponse.of(
                "Registration successful",
                authService.register(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     * Body: { "email": "...", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest request) {
        ApiResponse<AuthResponse> response = ApiResponse.of(
                "Login successful",
                authService.login(request));
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/refresh
     * Body: { "refreshToken": "..." }
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        ApiResponse<RefreshTokenResponse> response = ApiResponse.of(
                "Token refreshed successfully",
                authService.refresh(request.getRefreshToken()));
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/logout
     * Header: Authorization: Bearer <accessToken>
     * Body (optional): { "refreshToken": "..." }
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) RefreshTokenRequest request) {

        String accessToken  = extractBearer(authHeader);
        String refreshToken = (request != null) ? request.getRefreshToken() : null;

        authService.logout(accessToken, refreshToken);

        ApiResponse<Void> response = ApiResponse.of(HttpStatus.OK.value(), "Logged out successfully", null);
        return ResponseEntity.ok(response);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String extractBearer(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
