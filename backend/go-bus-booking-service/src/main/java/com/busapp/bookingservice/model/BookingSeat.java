package com.busapp.bookingservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "\"BookingSeat\"",
        indexes = {
                @Index(name = "idx_bookingseat_booking",      columnList = "bookingId"),
                @Index(name = "idx_bookingseat_seat",         columnList = "seatId"),
                @Index(name = "idx_bookingseat_booking_seat", columnList = "bookingId, seatId")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"bookingId\"", nullable = false)
    private Booking booking;

    /** References Seat from bus-service (cross-service, no FK). */
    @Column(name = "\"seatId\"", nullable = false)
    private Long seatId;

    @Column(name = "\"passengerNumber\"", length = 50)
    private Long passengerNumber;
}
