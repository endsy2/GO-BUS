package com.busapp.userservice.dto.request;

import com.busapp.userservice.model.enums.TransactionStatus;
import com.busapp.userservice.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFilterRequest {
    private UUID walletId;
    private TransactionType type;
    private TransactionStatus status;
    private String referenceId;

    // Transaction date range (inclusive), matched against createdAt.
    private LocalDate fromDate;
    private LocalDate toDate;

    // Transaction amount range (inclusive).
    private Double minAmount;
    private Double maxAmount;
}
