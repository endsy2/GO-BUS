package com.busapp.bookingservice.model;

import com.busapp.bookingservice.model.enums.BookingStatus;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "\"Booking\"",
        indexes = {
                @Index(name = "idx_booking_user",           columnList = "userId"),
                @Index(name = "idx_booking_schedule",       columnList = "scheduleId"),
                @Index(name = "idx_booking_status",         columnList = "bookingStatus"),
                @Index(name = "idx_booking_payment_status", columnList = "paymentStatus"),
                @Index(name = "idx_booking_created",        columnList = "createdAt"),
                @Index(name = "idx_booking_user_status",    columnList = "userId, bookingStatus"),
                @Index(name = "idx_booking_schedule_status", columnList = "scheduleId, bookingStatus"),
                @Index(name = "idx_booking_payment_method", columnList = "paymentMethod"),
                @Index(name = "idx_booking_amount",         columnList = "totalAmount"),
                @Index(name = "idx_booking_deleted",        columnList = "isDeleted"),
                @Index(name = "idx_booking_composite",      columnList = "isDeleted, bookingStatus, paymentStatus, createdAt")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** References user from user-service (cross-service, no FK). */
    @Column(name = "\"userId\"", nullable = false)
    private Long userId;

    /** References BusSchedule from bus-service (cross-service, no FK). */
    @Column(name = "\"scheduleId\"", nullable = false)
    private Long scheduleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"bookingStatus\"", columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    /** Total amount for booking */
    @Column(name = "\"totalAmount\"", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    /** Optional promo code applied */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"promoId\"")
    private PromoCode promo;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"paymentStatus\"", nullable = false, columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"paymentMethod\"", columnDefinition = "VARCHAR(50)")
    private PaymentMethodType paymentMethod;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "\"updatedAt\"")
    private LocalDateTime updatedAt;

    /** Soft delete */
    @Column(name = "\"isDeleted\"", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "\"deletedAt\"")
    private LocalDateTime deletedAt;

    /** Departure datetime copied from the schedule at booking time, used for INCOMING/PASS filtering. */
    @Column(name = "\"departureAt\"")
    private LocalDateTime departureAt;

    /** Customer phone number */
    @Column(name = "\"phoneNumber\"", length = 20)
    private String phoneNumber;

    /** Booking seats */
    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<BookingSeat> seats;

    /** Payment record */
    @OneToOne(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Payment payment;

    /** Ticket generated for this booking */
    @OneToOne(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Ticket ticket;

    /** Refund for this booking (at most one) */
    @OneToOne(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Refund refund;

    /** Promo usage history */
    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PromoUsage> promoUsages;
}