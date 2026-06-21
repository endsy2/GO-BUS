package com.busapp.busservice.model;

import com.busapp.busservice.model.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Represents seat availability for a specific schedule.
 * This entity tracks which seats are available/booked for each bus schedule,
 * allowing the same physical seat to be booked independently across different schedules.
 */
@Entity
@Table(name = "\"ScheduleSeat\"",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_schedule_seat", 
                                columnNames = {"scheduleId", "seatId"})
        },
        indexes = {
                @Index(name = "idx_scheduleseat_schedule", columnList = "scheduleId"),
                @Index(name = "idx_scheduleseat_seat", columnList = "seatId"),
                @Index(name = "idx_scheduleseat_status", columnList = "status"),
                @Index(name = "idx_scheduleseat_schedule_status", 
                       columnList = "scheduleId, status"),
                @Index(name = "idx_scheduleseat_booking", columnList = "bookingId")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSeat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"scheduleId\"", nullable = false)
    private BusSchedule schedule;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"seatId\"")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Seat seat;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;
    
    /**
     * Reference to booking if this seat is booked for this schedule.
     * Null if seat is available.
     */
    @Column(name = "\"bookingId\"")
    private Long bookingId;
    
    /**
     * User ID who has selected this seat (for PENDING status).
     * Used to track temporary seat selections before booking.
     */
    @Column(name = "\"pendingUserId\"")
    private Long pendingUserId;
    
    /**
     * Timestamp when seat was marked as PENDING.
     * Used for auto-expiry of seat selections.
     */
    @Column(name = "\"pendingAt\"")
    private java.time.LocalDateTime pendingAt;
}
