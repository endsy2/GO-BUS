package com.busapp.busservice.mapper;

import com.busapp.busservice.dto.ScheduleResponse;
import com.busapp.busservice.model.BusSchedule;
import com.busapp.busservice.model.enums.SeatStatus;
import com.busapp.busservice.service.ScheduleSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BusScheduleMapper {
    private final RouteMapper routeMapper;
    private final ScheduleSeatService scheduleSeatService;
    
    public ScheduleResponse toResponse(BusSchedule s) {
        return toResponse(s, true);
    }
    
    public ScheduleResponse toResponse(BusSchedule s, boolean includeSeatInfo) {
        ScheduleResponse.ScheduleResponseBuilder builder = ScheduleResponse.builder()
                .id(s.getId())
                .busId(s.getBus() != null ? s.getBus().getId() : null)
                .busNumber(s.getBus() != null ? s.getBus().getBusNumber() : null)
                .price(s.getPrice())
                .route(s.getBus()!=null?routeMapper.toResponse(s.getBus().getRoute()):null)
                .departureDateTime(s.getDepartureDateTime())
                .arrivalDateTime(s.getArrivalDateTime())
                .bookingIds(s.getBookingIds());
        
        // Add seat availability information if requested
        if (includeSeatInfo) {
            builder.seatAvailability(getSeatAvailability(s.getId()));
        }
        
        return builder.build();
    }
    
    private ScheduleResponse.SeatAvailability getSeatAvailability(Long scheduleId) {
        try {
            Map<SeatStatus, Long> seatCounts = scheduleSeatService.getSeatCountsByStatus(scheduleId);
            
            long available = seatCounts.getOrDefault(SeatStatus.AVAILABLE, 0L);
            long booked = seatCounts.getOrDefault(SeatStatus.BOOKED, 0L);
            long unavailable = seatCounts.getOrDefault(SeatStatus.UNAVAILABLE, 0L);
            long total = available + booked + unavailable;
            
            return ScheduleResponse.SeatAvailability.builder()
                    .totalSeats((int) total)
                    .availableSeats((int) available)
                    .bookedSeats((int) booked)
                    .unavailableSeats((int) unavailable)
                    .build();
        } catch (Exception e) {
            // Return null if seat info cannot be retrieved
            return null;
        }
    }
    
    public List<ScheduleResponse> toListReponseList(List<BusSchedule> scheduleList) {
        return scheduleList.stream().map(this::toResponse).toList();
    }
    
    public Page<ScheduleResponse> toPageResponse(Page<BusSchedule> schedules) {
        return schedules.map(this::toResponse);
    }
}
