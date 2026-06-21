package com.busapp.userservice.model;

import com.busapp.userservice.model.enums.PaymentMethodType;
import com.busapp.userservice.model.enums.TopUpStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"TopUp\"",
        indexes = {
                @Index(name = "idx_topup_user",   columnList = "userId"),
                @Index(name = "idx_topup_status", columnList = "status")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"userId\"", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"paymentMethod\"", nullable = false, columnDefinition = "VARCHAR(50)")
    private PaymentMethodType paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private TopUpStatus status = TopUpStatus.PENDING;

    @Column(name = "\"paymentGateway\"", length = 100)
    private String paymentGateway;

    @Column(name = "\"transactionId\"")
    private String transactionId;

    @Column(name = "\"completedAt\"")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
