package com.busapp.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatOccupancyReportResponse {
    private Long scheduleId;
    private Long routeId;
    private Long busId;
    private Integer totalSeats;
    private Integer bookedSeats;
    private Double occupancyRate;
}
