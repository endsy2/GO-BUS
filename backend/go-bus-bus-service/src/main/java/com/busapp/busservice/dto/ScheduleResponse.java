package com.busapp.busservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private Long id;
    private Long busId;
    private String busNumber;
    private Double price;
    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;
    private RouteResponse route;
    private String bookingIds;
    
    // Seat availability information
    private SeatAvailability seatAvailability;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatAvailability {
        private Integer totalSeats;
        private Integer availableSeats;
        private Integer bookedSeats;
        private Integer unavailableSeats;
    }
}
