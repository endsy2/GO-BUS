package com.busapp.bookingservice.model;

import com.busapp.bookingservice.model.enums.DiscountType;
import com.busapp.bookingservice.model.enums.PromoStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "\"PromoCode\"",
        indexes = {
                @Index(name = "idx_promo_code",        columnList = "code"),
                @Index(name = "idx_promo_status",      columnList = "status"),
                @Index(name = "idx_promo_valid",       columnList = "validFrom, validTo"),
                @Index(name = "idx_promo_code_status", columnList = "code, status")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"discountType\"", nullable = false, columnDefinition = "VARCHAR(50)")
    private DiscountType discountType;

    @Column(name = "\"discountValue\"", nullable = false)
    private Double discountValue;

    @Column(name = "\"maxUses\"")
    private Integer     maxUses;

    @Column(name = "\"usedCount\"", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "\"validFrom\"")
    private LocalDateTime validFrom;

    @Column(name = "\"validTo\"")
    private LocalDateTime validTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private PromoStatus status = PromoStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "\"updatedAt\"", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "promo", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Booking> bookings;

    @OneToMany(mappedBy = "promo", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PromoUsage> promoUsages;
}
