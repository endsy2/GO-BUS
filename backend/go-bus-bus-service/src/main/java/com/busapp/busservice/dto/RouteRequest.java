package com.busapp.busservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {

    @NotBlank(message = "Origin must not be blank")
    @Size(max = 100, message = "Origin must not exceed 100 characters")
    private String origin;

    @NotBlank(message = "Destination must not be blank")
    @Size(max = 100, message = "Destination must not exceed 100 characters")
    private String destination;

    @Positive(message = "Distance must be a positive number")
    private Double distanceKm;

    @Positive(message = "Duration must be a positive number")
    private Integer durationMinutes;

    /** Optional origin location coordinates stored as a JSON string. */
    private String originLocation;

    /** Optional destination location coordinates stored as a JSON string. */
    private String destinationLocation;
}
