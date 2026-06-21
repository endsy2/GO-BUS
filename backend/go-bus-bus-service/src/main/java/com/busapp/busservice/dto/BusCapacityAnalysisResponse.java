package com.busapp.busservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusCapacityAnalysisResponse {
    private Long busId;
    private String busNumber;
    private String plate;
    private String routeName;
    private Integer totalSeats;
    private Integer totalSchedules;
    private Integer fullyBookedCount;
    private Double fullyBookedPercentage;
    private Double averageOccupancyRate;
    private Long totalBookedSeats;
}
