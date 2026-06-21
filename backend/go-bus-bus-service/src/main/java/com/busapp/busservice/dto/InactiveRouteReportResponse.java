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
public class InactiveRouteReportResponse {
    private Long routeId;
    private String origin;
    private String destination;
    private Double distanceKm;
    private Integer totalBuses;
    private Integer totalSchedules;
    private Long totalBookings;
    private LocalDateTime lastScheduleDate;
    private String inactivityReason;
}
