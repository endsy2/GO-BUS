package com.busapp.userservice.controller;

import com.busapp.userservice.dto.admin.PagedResponse;
import com.busapp.userservice.dto.request.TransactionFilterRequest;
import com.busapp.userservice.dto.request.WalletLoginRequest;
import com.busapp.userservice.dto.request.WalletTransactionRequest;
import com.busapp.userservice.dto.response.ApiResponse;
import com.busapp.userservice.dto.response.WalletBalanceResponse;
import com.busapp.userservice.dto.response.WalletResponse;
import com.busapp.userservice.dto.response.WalletTransactionDetailResponse;
import com.busapp.userservice.dto.response.WalletTransactionResponse;
import com.busapp.userservice.model.enums.TransactionStatus;
import com.busapp.userservice.model.enums.TransactionType;
import com.busapp.userservice.service.WalletService;
import com.busapp.userservice.service.WalletSessionService;
import com.busapp.userservice.util.UserUtil;
import com.busapp.userservice.util.WalletSessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;
    private final WalletSessionService walletSessionService;
    private final WalletSessionUtil walletSessionUtil;
    private final UserUtil userUtil;

    /**
     * Create wallet for current user - No wallet session required
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @RequestBody WalletLoginRequest walletLoginRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        "Wallet created successfully",
                        walletService.createWallet(walletLoginRequest)));
    }

    /**
     * Login to wallet with PIN code - No wallet session required
     * Returns wallet session token for subsequent operations
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<WalletResponse>> login(
            @RequestBody WalletLoginRequest walletLoginRequest) {
        return ResponseEntity.ok(
                ApiResponse.of(
                        "Wallet login successful",
                        walletService.walletLogin(walletLoginRequest)
                ));
    }

    /**
     * Logout from wallet - Invalidates wallet session
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        Long currentUserId = userUtil.getCurrentUserId();
        walletSessionService.invalidateWalletSession(currentUserId);
        return ResponseEntity.ok(
                ApiResponse.of(
                        "Wallet logout successful",
                        null
                ));
    }

    /**
     * Get current user's wallet - Requires wallet session
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<WalletResponse>> getCurrentUserWallet() {

        return ResponseEntity.ok(
                ApiResponse.of(
                        "Wallet retrieved successfully",
                        walletService.userCurrentWallet()
                ));
    }
    @GetMapping("my-balance")
    public ResponseEntity<ApiResponse<WalletBalanceResponse>>getCurrentUserWalletBalance() {
        Long userId=userUtil.getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.of(
                        "Wallet retrieved successfully",
                        walletService.getCurrentUserBalance(userId)

                )
        );
    }

    /**
     * Get current user's transactions - Requires wallet session
     */
    @GetMapping("/me/transactions")
    public ResponseEntity<PagedResponse<WalletTransactionResponse>> getMyTransactions(
            @RequestHeader("X-Wallet-Session") String walletSessionToken,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        walletSessionUtil.validateAndRefreshSession(walletSessionToken);

        Long currentUserId = userUtil.getCurrentUserId();
        
        // Get user's wallet first
        WalletResponse wallet = walletService.getWalletByUserId(currentUserId);

        TransactionFilterRequest filter = TransactionFilterRequest.builder()
                .walletId(wallet.getId())
                .type(type)
                .status(status)
                .build();

        return ResponseEntity.ok(walletService.getTransactions(filter, page, size));
    }



}
