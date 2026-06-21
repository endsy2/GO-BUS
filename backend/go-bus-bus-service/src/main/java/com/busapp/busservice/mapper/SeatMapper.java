package com.busapp.busservice.mapper;

import com.busapp.busservice.dto.SeatResponse;
import com.busapp.busservice.model.Bus;
import com.busapp.busservice.model.Seat;
import com.busapp.busservice.model.enums.SeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SeatMapper {

    public SeatResponse toResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .busId(seat.getBus() != null ? seat.getBus().getId() : null)
                .seatNumber(seat.getSeatNumber())
                .build();
    }

    public List<SeatResponse> toResponseList(List<Seat> seats) {
        return seats.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
