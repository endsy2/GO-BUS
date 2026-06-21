package com.busapp.busservice.model;

import com.busapp.busservice.model.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Represents a physical seat on a bus.
 * Seat availability for booking is tracked per schedule via ScheduleSeat entity.
 */
@Entity
@Table(name = "\"Seat\"",
        indexes = {
                @Index(name = "idx_seat_bus",        columnList = "busId"),
                @Index(name = "idx_seat_number",     columnList = "seatNumber")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"busId\"", nullable = false)
    private Bus bus;

    @Column(name = "\"seatNumber\"", length = 10)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"seatType\"", nullable = false, columnDefinition = "VARCHAR(50)")
    private SeatType seatType;

    @OneToMany(mappedBy = "seat")
    private List<ScheduleSeat> scheduleSeats;
}
