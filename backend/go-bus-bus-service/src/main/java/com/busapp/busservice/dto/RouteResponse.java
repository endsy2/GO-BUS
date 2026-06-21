package com.busapp.busservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    private Long   id;
    private String origin;
    private String destination;
    private Double  distanceKm;
    private Integer durationMinutes;
    private String  originLocation;
    private String  destinationLocation;
    /** Number of buses currently assigned to this route. */
    private int     busCount;
}
