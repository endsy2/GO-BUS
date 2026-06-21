package com.busapp.busservice.dto;

import com.busapp.busservice.model.enums.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatRequest {
    @NotNull
    private Long busId;
    @NotBlank
    private String seatNumber;
    @NotNull
    private SeatType seatType;
    private String positionLabel;
}
