package com.busapp.busservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatAvailabilityEvent {
    private String type;           // SEAT_SELECTED, SEAT_DESELECTED, SEAT_BOOKED, SEAT_RELEASED, SEAT_SELECTION_EXPIRED
    private Long scheduleId;
    private Long seatId;
    private String seatNumber;
    private Long bookingId;
    private String status;         // AVAILABLE, PENDING, BOOKED
    private Long userId;           // User who selected/deselected the seat (null for system events)
    private LocalDateTime timestamp;
}
