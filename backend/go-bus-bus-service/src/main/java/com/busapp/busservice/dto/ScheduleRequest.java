package com.busapp.busservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {
    @NotNull
    private Long busId;
    private Double price;
    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;
}
