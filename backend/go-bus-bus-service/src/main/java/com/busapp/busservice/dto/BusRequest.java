package com.busapp.busservice.dto;

import com.busapp.busservice.model.enums.BusStatus;
import com.busapp.busservice.model.enums.BusType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BusRequest {

    @NotNull(message = "Route ID is required")
    private Long routeId;

    @NotBlank(message = "Bus number is required")
    private String busNumber;

    @NotNull(message = "Bus type is required")
    private BusType busType;

    /** Optional layout ID. If provided, totalSeats will be auto-calculated from layout. */
    private Long layoutId;

    private String plate;

    private String model;

    private BusStatus busStatus;
}
