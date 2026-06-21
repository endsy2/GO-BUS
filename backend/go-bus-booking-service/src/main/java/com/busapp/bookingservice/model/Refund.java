package com.busapp.bookingservice.model;

import com.busapp.bookingservice.model.enums.RefundMethod;
import com.busapp.bookingservice.model.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"Refund\"",
        indexes = {
                @Index(name = "idx_refund_booking", columnList = "bookingId"),
                @Index(name = "idx_refund_status", columnList = "status"),
                @Index(name = "idx_refund_created", columnList = "createdAt")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"bookingId\"", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "\"amount\"", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "\"reason\"", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"status\"", nullable = false, columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private RefundStatus status = RefundStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"refundMethod\"", nullable = false, columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private RefundMethod refundMethod=RefundMethod.MANUAL ;

    @Column(name = "\"adminNote\"", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "\"processedBy\"")
    private Long processedBy;

    @Column(name = "\"processedAt\"")
    private LocalDateTime processedAt;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "\"updatedAt\"")
    private LocalDateTime updatedAt;
}
