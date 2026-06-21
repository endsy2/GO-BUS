package com.busapp.busservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for seat selection/deselection via WebSocket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatSelectionRequest {
    private Long scheduleId;
    private Long seatId;
    private Long userId;
}
