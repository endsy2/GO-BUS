package com.busapp.busservice.dto;

import com.busapp.busservice.model.enums.SeatStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusRequest {
    @NotNull
    private SeatStatus status;
}
