package com.busapp.bookingservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"PromoUsage\"",
        uniqueConstraints = @UniqueConstraint(name = "unique_promo_booking",
                columnNames = {"\"promoId\"", "\"bookingId\""}),
        indexes = {
                @Index(name = "idx_promousage_promo",   columnList = "promoId"),
                @Index(name = "idx_promousage_user",    columnList = "userId"),
                @Index(name = "idx_promousage_booking", columnList = "bookingId")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"promoId\"", nullable = false)
    private PromoCode promo;

    /** Cross-service reference to user-service. */
    @Column(name = "\"userId\"", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"bookingId\"", nullable = false)
    private Booking booking;

    @CreationTimestamp
    @Column(name = "\"usedAt\"", nullable = false, updatable = false)
    private LocalDateTime usedAt;
}
