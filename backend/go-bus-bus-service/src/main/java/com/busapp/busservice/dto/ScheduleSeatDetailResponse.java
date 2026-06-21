package com.busapp.busservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Detailed response for schedule seats including full bus information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSeatDetailResponse {
    private Long id;
    private String busNumber;
    private String plate;
    private String model;
    private String busType;
    private String status;
    private Integer totalSeats;
    private RouteResponse route;
    private LayoutResponse layout;
    private List<ScheduleSeatResponse> seats;
}
