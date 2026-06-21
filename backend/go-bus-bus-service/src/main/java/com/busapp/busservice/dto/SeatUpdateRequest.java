package com.busapp.busservice.dto;

import com.busapp.busservice.model.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatUpdateRequest {
    private String   seatNumber;
    private SeatType seatType;
    private String   positionLabel;
}
