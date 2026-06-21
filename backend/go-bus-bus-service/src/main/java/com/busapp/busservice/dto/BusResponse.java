package com.busapp.busservice.dto;

import com.busapp.busservice.model.enums.BusStatus;
import com.busapp.busservice.model.enums.BusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusResponse {
    private Long      id;
    private String    busNumber;
    private String    plate;
    private String    model;
    private BusType   busType;
    private BusStatus status;
    private int       totalSeats;
    private RouteResponse route;
    private Long      layoutId;
}
