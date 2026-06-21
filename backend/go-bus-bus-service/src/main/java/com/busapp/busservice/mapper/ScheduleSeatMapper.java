package com.busapp.busservice.mapper;

import com.busapp.busservice.dto.ScheduleSeatDetailResponse;
import com.busapp.busservice.dto.ScheduleSeatResponse;
import com.busapp.busservice.model.BusSchedule;
import com.busapp.busservice.model.ScheduleSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScheduleSeatMapper {

    private final SeatMapper seatMapper;
    private final BusLayoutMapper busLayoutMapper;
    private final RouteMapper routeMapper;

    public ScheduleSeatResponse toResponse(ScheduleSeat scheduleSeat) {
        return ScheduleSeatResponse.builder()
                .id(scheduleSeat.getSeat().getId())
                .seatNumber(scheduleSeat.getSeat().getSeatNumber())
                .status(scheduleSeat.getStatus())
                .bookingId(scheduleSeat.getBookingId())
                .pendingUserId(scheduleSeat.getPendingUserId())
                .pendingAt(scheduleSeat.getPendingAt())
                .build();
    }

    public List<ScheduleSeatResponse> toResponseList(List<ScheduleSeat> scheduleSeats) {
        return scheduleSeats.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ScheduleSeatDetailResponse toDetailResponse(BusSchedule schedule, List<ScheduleSeat> scheduleSeats) {
        return ScheduleSeatDetailResponse.builder()
                .id(schedule.getBus().getId())
                .busNumber(schedule.getBus().getBusNumber())
                .plate(schedule.getBus().getPlate())
                .model(schedule.getBus().getModel())
                .busType(schedule.getBus().getBusType().name())
                .status(schedule.getBus().getStatus().name())
                .totalSeats(schedule.getBus().getTotalSeats())
                .route(routeMapper.toResponse(schedule.getBus().getRoute()))
                .layout(busLayoutMapper.toResponse(schedule.getBus().getLayout()))
                .seats(toResponseList(scheduleSeats))
                .build();
    }
}
