package com.busapp.busservice.dto;

import ch.qos.logback.core.Layout;
import com.busapp.busservice.model.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSeatResponse {
    private Long id;
    private String seatNumber;
    private SeatStatus status;
    private Long bookingId;
    private Long pendingUserId;
    private java.time.LocalDateTime pendingAt;
}