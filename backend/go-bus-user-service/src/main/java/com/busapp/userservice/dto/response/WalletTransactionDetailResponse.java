package com.busapp.userservice.dto.response;

import com.busapp.userservice.model.enums.Currency;
import com.busapp.userservice.model.enums.TransactionStatus;
import com.busapp.userservice.model.enums.TransactionType;
import com.busapp.userservice.model.enums.WalletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Detailed view of a single wallet transaction, enriched with the owning
 * wallet's current state and the wallet owner's basic info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionDetailResponse {

    // --- Transaction ---
    private Long              id;
    private String            referenceId;
    private TransactionType   type;
    private TransactionStatus status;
    private Double            amount;
    private Double            balanceBefore;
    private Double            balanceAfter;
    private String            description;
    private String            metadata;
    private LocalDateTime     createdAt;
    private LocalDateTime     completedAt;

    // --- Wallet ---
    private WalletInfo wallet;

    // --- Owner ---
    private OwnerInfo owner;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletInfo {
        private UUID         id;
        private Double       currentBalance;
        private Currency     currency;
        private WalletStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerInfo {
        private Long   userId;
        private String userName;
        private String fullName;
        private String email;
        private String phone;
    }
}
