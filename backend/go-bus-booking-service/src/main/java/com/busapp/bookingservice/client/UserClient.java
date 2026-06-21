package com.busapp.bookingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Feign client to communicate with user-service via Eureka load-balancer.
 */
@FeignClient(
        name = "user-service",
        url = "${external.user-service.url}"

)
public interface UserClient {

    @GetMapping("/api/users/{id}/basic")
    ResponseEntity<UserApiResponse<UserBasicInfo>> getUserBasicInfoById(@PathVariable("id") Long id);

    /**
     * Batch fetch basic user info - optimized endpoint that only selects 5 fields
     * Uses native query to avoid N+1 problem
     */
    @PostMapping("/api/users/batch/basic")
    ResponseEntity<UserApiResponse<List<UserBasicInfo>>> getUsersByIds(@RequestBody Set<Long> userIds);

    /**
     * Get wallet information by user ID
     */
    @GetMapping("/api/wallets/user/{userId}")
    ResponseEntity<UserApiResponse<WalletInfo>> getWalletByUserId(@PathVariable("userId") Long userId);

    record UserApiResponse<T>(
            Integer status,
            String endpoint,
            String message,
            T data
    ) {}

    /**
     * Resolve a single user by exact username. Returns 200 with null {@code data}
     * when no user matches (the user-service endpoint never throws 404 here).
     */
    @GetMapping("/api/users/username/{username}")
    ResponseEntity<UserApiResponse<UserBasicInfo>> getUserByUsername(
            @PathVariable("username") String username
    );


    /**
     * Basic user info matching UserBasicResponse from user-service
     * Fields: id, userName, fullName, email, phone
     */
    record UserBasicInfo(
            Long   id,
            String userName,
            String fullName,
            String email,
            String phone
    ) {}

    record WalletInfo(
            String walletId,
            Double balance,
            String currency,
            String status
    ) {}

    @PostMapping("/api/wallets/do-transaction/{userId}")
    ResponseEntity<UserApiResponse<WalletTransactionResponse>> getWallet(
            @PathVariable("userId") Long userId,
            @RequestHeader("X-Wallet-Session") String walletSessionToken,
            @RequestParam("transaction-type") TransactionType transactionType,
            @RequestParam("amount") Double amount
    );

    @PostMapping("/api/wallets/internal/refund/{userId}")
    ResponseEntity<UserApiResponse<WalletTransactionResponse>> refundWallet(
            @PathVariable("userId") Long userId,
            @RequestParam("amount") Double amount,
            @RequestParam("description") String description
    );

    record WalletTransactionResponse (
            Long              id,
            UUID              walletId,
            Double            amount,
            TransactionType   type,
            TransactionStatus status,
            String            referenceId,
            String            description,
            Double            balanceBefore,
            Double            balanceAfter,
            String            metadata,
            LocalDateTime     createdAt,
            LocalDateTime     completedAt
    ){}
    enum TransactionType {
        TOP_UP, PAYMENT, REFUND, WITHDRAWAL, BONUS
    }
    enum TransactionStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }
}
