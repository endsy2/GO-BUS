package com.busapp.userservice.dto.response;

import com.busapp.userservice.model.enums.TransactionStatus;
import com.busapp.userservice.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionResponse {
    private Long              id;
    private UUID              walletId;
    private Double            amount;
    private TransactionType   type;
    private TransactionStatus status;
    private String            referenceId;
    private String            description;
    private Double            balanceBefore;
    private Double            balanceAfter;
    private String            metadata;
    private LocalDateTime     createdAt;
    private LocalDateTime     completedAt;
}
