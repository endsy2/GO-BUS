package com.busapp.userservice.controller;

import com.busapp.userservice.dto.response.ApiResponse;
import com.busapp.userservice.dto.response.WalletResponse;
import com.busapp.userservice.dto.response.WalletTransactionResponse;
import com.busapp.userservice.model.enums.TransactionType;
import com.busapp.userservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal wallet controller for inter-service communication
 * Used by booking-service to process wallet payments
 */
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Slf4j
public class InternalWalletController {

    private final WalletService walletService;

    /**
     * Internal endpoint for booking-service to fetch a user's wallet info.
     * Called by booking-service via Feign client (UserClient#getWalletByUserId)
     * to build wallet snapshots (e.g. refund details).
     *
     * The response field names (walletId/balance/currency/status) intentionally
     * match the booking-service {@code UserClient.WalletInfo} record.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<WalletInfoResponse>> getWalletByUserId(
            @PathVariable("userId") Long userId) {

        log.info("[INTERNAL WALLET] Fetching wallet info - userId={}", userId);

        WalletResponse wallet = walletService.getWalletByUserId(userId);

        WalletInfoResponse info = new WalletInfoResponse(
                wallet.getId() != null ? wallet.getId().toString() : null,
                wallet.getBalance(),
                wallet.getCurrency() != null ? wallet.getCurrency().name() : null,
                wallet.getStatus() != null ? wallet.getStatus().name() : null
        );

        return ResponseEntity.ok().body(
                ApiResponse.of(
                        HttpStatus.OK.value(),
                        "Wallet retrieved successfully",
                        info
                )
        );
    }

    /**
     * Lightweight wallet projection for inter-service calls.
     */
    public record WalletInfoResponse(
            String walletId,
            Double balance,
            String currency,
            String status
    ) {}

    /**
     * Internal endpoint for booking-service to deduct wallet for payments
     * Requires wallet session validation
     * 
     * This endpoint is called by booking-service via Feign client
     */
    @PostMapping("/do-transaction/{userId}")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> doTransactionInternal(
            @PathVariable("userId") Long userId,
            @RequestHeader("X-Wallet-Session") String walletSessionToken,
            @RequestParam("transaction-type") TransactionType transactionType,
            @RequestParam("amount") Double amount) {

        log.info("[INTERNAL WALLET] Processing transaction - userId={}, type={}, amount={}", 
                userId, transactionType, amount);

        return ResponseEntity.ok().body(
                ApiResponse.of(
                        HttpStatus.OK.value(),
                        "Transaction completed successfully",
                        walletService.doTransactionInternal(userId, walletSessionToken, transactionType, amount)
                )
        );
    }

    /**
     * Internal endpoint for booking-service to process refunds
     * Does NOT require wallet session - called by admin approval
     * 
     * This endpoint is called by booking-service via Feign client
     */
    @PostMapping("/internal/refund/{userId}")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> refundWallet(
            @PathVariable("userId") Long userId,
            @RequestParam("amount") Double amount,
            @RequestParam("description") String description) {

        log.info("[INTERNAL WALLET REFUND] Processing refund - userId={}, amount={}, description={}", 
                userId, amount, description);

        return ResponseEntity.ok().body(
                ApiResponse.of(
                        HttpStatus.OK.value(),
                        "Refund processed successfully",
                        walletService.refundTransaction(userId, amount, description)
                )
        );
    }
}
