package com.busapp.userservice.controller;

import com.busapp.userservice.dto.admin.PagedResponse;
import com.busapp.userservice.dto.request.TransactionFilterRequest;
import com.busapp.userservice.dto.request.WalletFilterRequest;
import com.busapp.userservice.dto.request.WalletTransactionRequest;
import com.busapp.userservice.dto.response.ApiResponse;
import com.busapp.userservice.dto.response.WalletResponse;
import com.busapp.userservice.dto.response.WalletTransactionDetailResponse;
import com.busapp.userservice.dto.response.WalletTransactionResponse;
import com.busapp.userservice.exception.UnauthorizedException;
import com.busapp.userservice.model.enums.TransactionStatus;
import com.busapp.userservice.model.enums.TransactionType;
import com.busapp.userservice.model.enums.WalletStatus;
import com.busapp.userservice.security.UserPrincipal;
import com.busapp.userservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/wallets")
@RequiredArgsConstructor
@Slf4j
public class AdminWalletController {

    private final WalletService walletService;

    /**
     * Get wallet by wallet ID - ADMIN only
     */
    @GetMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletById(
            @PathVariable UUID walletId) {

        return ResponseEntity.ok(ApiResponse.of(
                "Wallet retrieved successfully",
                walletService.getWalletById(walletId)));
    }

    /**
     * Get wallet by user ID - ADMIN only
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(ApiResponse.of(
                "Wallet retrieved successfully",
                walletService.getWalletByUserId(userId)));
    }

    /**
     * Create (provision) a wallet for a user - ADMIN only.
     * The wallet is created with no PIN; the user sets it on first wallet login.
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<WalletResponse>> createWalletForUser(
            @PathVariable Long userId) {

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Wallet created successfully",
                walletService.createWalletForUser(userId)));
    }

    /**
     * Get all wallets with filters - ADMIN only
     */
    @GetMapping
    public ResponseEntity<PagedResponse<WalletResponse>> getWallets(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) WalletStatus status,
            @RequestParam(required = false) Double minBalance,
            @RequestParam(required = false) Double maxBalance,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {



        WalletFilterRequest filter = WalletFilterRequest.builder()
                .userId(userId)
                .name(name)
                .status(status)
                .minBalance(minBalance)
                .maxBalance(maxBalance)
                .currency(currency)
                .build();

        return ResponseEntity.ok(walletService.getWallets(filter, page, size));
    }

    /**
     * Get user's transactions - ADMIN only
     */
    @GetMapping("/user/{userId}/transactions")
    public ResponseEntity<PagedResponse<WalletTransactionResponse>> getUserTransactions(
            @PathVariable Long userId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Get user's wallet first
        WalletResponse wallet = walletService.getWalletByUserId(userId);

        TransactionFilterRequest filter = TransactionFilterRequest.builder()
                .walletId(wallet.getId())
                .type(type)
                .status(status)
                .referenceId(referenceId)
                .fromDate(fromDate)
                .toDate(toDate)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .build();

        return ResponseEntity.ok(walletService.getTransactions(filter, page, size));
    }

    /**
     * Get transactions by wallet ID - ADMIN only
     */
    @GetMapping("/transactions")
    public ResponseEntity<PagedResponse<WalletTransactionResponse>> getTransactions(
            @RequestParam(required = false) UUID walletId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        TransactionFilterRequest filter = TransactionFilterRequest.builder()
                .walletId(walletId)
                .type(type)
                .status(status)
                .referenceId(referenceId)
                .fromDate(fromDate)
                .toDate(toDate)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .build();

        return ResponseEntity.ok(walletService.getTransactions(filter, page, size));
    }
    /**
     * Get a single transaction detail by reference number
     */
    @GetMapping("/transactions/{referenceId}")
    public ResponseEntity<ApiResponse<WalletTransactionDetailResponse>> getTransactionByReferenceId(
            @PathVariable String referenceId) {
        return ResponseEntity.ok(
                ApiResponse.of(
                        "Transaction retrieved successfully",
                        walletService.getTransactionByReferenceId(referenceId)
                ));
    }

}
