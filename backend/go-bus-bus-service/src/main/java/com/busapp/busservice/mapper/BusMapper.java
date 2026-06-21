package com.busapp.busservice.mapper;

import com.busapp.busservice.dto.BusDetailResponse;
import com.busapp.busservice.dto.BusRequest;
import com.busapp.busservice.dto.BusResponse;
import com.busapp.busservice.model.Bus;
import com.busapp.busservice.model.BusLayout;
import com.busapp.busservice.model.BusRoute;
import com.busapp.busservice.model.enums.BusStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class BusMapper {

    private final RouteMapper routeMapper;
    private final BusLayoutMapper busLayoutMapper;
    private final SeatMapper seatMapper;

    public BusMapper(RouteMapper routeMapper, BusLayoutMapper busLayoutMapper, SeatMapper seatMapper) {
        this.routeMapper = routeMapper;
        this.busLayoutMapper = busLayoutMapper;
        this.seatMapper = seatMapper;
    }

    public BusResponse toResponse(Bus bus) {
        BusResponse response = new BusResponse();
        response.setId(bus.getId());
        response.setBusNumber(bus.getBusNumber());
        response.setPlate(bus.getPlate());
        response.setModel(bus.getModel());
        response.setBusType(bus.getBusType());
        response.setStatus(bus.getStatus());
        response.setTotalSeats(bus.getTotalSeats());
        response.setRoute(bus.getRoute() != null ? routeMapper.toResponse(bus.getRoute()) : null);
        response.setLayoutId(bus.getLayout() != null ? bus.getLayout().getId() : null);
        return response;
    }

    public Bus toEntity(BusRequest request, BusRoute route, BusLayout layout) {
        return Bus.builder()
                .route(route)
                .busNumber(request.getBusNumber())
                .plate(request.getPlate())
                .model(request.getModel())
                .busType(request.getBusType())
                .status(request.getBusStatus() != null ? request.getBusStatus() : BusStatus.Active)
                .layout(layout)
                .build();
    }
    public Page<BusResponse> toPageResponse(Page<Bus> buses) {
        return buses.map(this::toResponse);
    }

    public BusDetailResponse toResponseDetail(Bus bus) {
        return BusDetailResponse.builder()
                .id(bus.getId())
                .busNumber(bus.getBusNumber())
                .plate(bus.getPlate())
                .model(bus.getModel())
                .busType(bus.getBusType())
                .status(bus.getStatus())
                .totalSeats(bus.getTotalSeats())
                .route(routeMapper.toResponse(bus.getRoute()))
                .layout(busLayoutMapper.toResponse(bus.getLayout()))
                .seats(bus.getSeats() != null ? seatMapper.toResponseList(bus.getSeats()) : null)
                .build();
    }
}
