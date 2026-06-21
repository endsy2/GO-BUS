package com.busapp.bookingservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"Ticket\"",
        indexes = {
                @Index(name = "idx_ticket_booking",   columnList = "bookingId"),
                @Index(name = "idx_ticket_qr",        columnList = "qrCode"),
                @Index(name = "idx_ticket_issued_at", columnList = "issuedAt")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"bookingId\"", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "\"qrCode\"", columnDefinition = "TEXT")
    private String qrCode;

    @CreationTimestamp
    @Column(name = "\"issuedAt\"", nullable = false, updatable = false)
    private LocalDateTime issuedAt;
}
