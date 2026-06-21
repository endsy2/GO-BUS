package com.busapp.busservice.dto;

import com.busapp.busservice.model.enums.SeatType;
import com.busapp.busservice.model.repository.ScheduleSeatRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Seat entity.
 * Note: status and bookings are now tracked in ScheduleSeat table per schedule.
 * Use ScheduleSeatResponse to get seat availability for a specific schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    private Long id;
    private Long busId;
    private String seatNumber;
    private SeatType seatType;
    private String positionLabel;
    // Deprecated fields - kept for backward compatibility
    @Deprecated
    private Object status; // Use ScheduleSeat for status
    @Deprecated
    private Object bookings; // Use ScheduleSeat for bookings
}
