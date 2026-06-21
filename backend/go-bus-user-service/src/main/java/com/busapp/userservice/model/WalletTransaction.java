package com.busapp.userservice.model;

import com.busapp.userservice.model.enums.TransactionStatus;
import com.busapp.userservice.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"WalletTransaction\"",
        indexes = {
                @Index(name = "idx_transaction_wallet",    columnList = "walletId"),
                @Index(name = "idx_transaction_reference", columnList = "referenceId"),
                @Index(name = "idx_transaction_status",    columnList = "status")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"walletId\"", nullable = false)
    private UserWallet wallet;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "\"referenceId\"")
    private String referenceId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "\"balanceBefore\"", nullable = false)
    private Double balanceBefore;

    @Column(name = "\"balanceAfter\"", nullable = false)
    private Double balanceAfter;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "\"completedAt\"")
    private LocalDateTime completedAt;
}
