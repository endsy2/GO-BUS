package com.busapp.busservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusUtilizationReportResponse {
    private Long busId;
    private String busNumber;
    private String plate;
    private String model;
    private String busType;
    private String routeName;
    private Integer totalSchedules;
    private Long totalBookings;
    private Double utilizationRate;
    private String status;
}
