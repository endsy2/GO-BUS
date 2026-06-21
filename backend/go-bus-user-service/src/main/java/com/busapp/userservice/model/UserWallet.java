package com.busapp.userservice.model;

import com.busapp.userservice.model.enums.Currency;
import com.busapp.userservice.model.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@BatchSize(size = 25)   // batch-load wallets: 20 lazy hits → 1 IN-clause SELECT
@Table(name = "\"UserWallet\"",
        indexes = @Index(name = "idx_wallet_user", columnList = "userId"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWallet {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"userId\"", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Double balance = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;

    @Column(name = "\"lastTransaction\"")
    private LocalDateTime lastTransaction;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String pinCode;

    @UpdateTimestamp
    @Column(name = "\"updatedAt\"", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "wallet", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<WalletTransaction> transactions;
}
