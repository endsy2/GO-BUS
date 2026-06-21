package com.busapp.bookingservice.model;

import com.busapp.bookingservice.model.enums.Currency;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "\"Payment\"",
        indexes = {
                @Index(name = "idx_payment_booking",     columnList = "bookingId"),
                @Index(name = "idx_payment_transaction", columnList = "transactionId"),
                @Index(name = "idx_payment_status",      columnList = "status"),
                @Index(name = "idx_payment_method",      columnList = "method")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"bookingId\"", nullable = false, unique = true)
    private Booking booking;

    @Column
    private Double amount;

    @Column
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private PaymentMethodType method;

    @Column(name = "\"transactionId\"")
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "\"description\"",length = 200)
    private String description;

    @Column(name = "\"paidAt\"")
    private LocalDateTime paidAt;

    /** References WalletTransaction ID from user-service (cross-service, no FK). */
    @Column(name = "\"walletTransactionId\"")
    private UUID walletTransactionId;

}
