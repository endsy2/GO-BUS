package com.busapp.busservice.dto;

import com.busapp.busservice.model.enums.BusStatus;
import com.busapp.busservice.model.enums.BusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BusDetailResponse {
    private Long          id;
    private String        busNumber;
    private String        plate;
    private String        model;
    private BusType       busType;
    private BusStatus     status;
    private int           totalSeats;
    private RouteResponse  route;
    private LayoutResponse layout;
    private java.util.List<SeatResponse> seats;
}
