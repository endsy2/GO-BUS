package com.busapp.busservice.mapper;

import com.busapp.busservice.dto.RouteRequest;
import com.busapp.busservice.dto.RouteResponse;
import com.busapp.busservice.model.BusRoute;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class RouteMapper {

    /** Entity → response DTO (with bus count derived from the buses collection). */
    public RouteResponse toResponse(BusRoute route) {
        return RouteResponse.builder()
                .id(route.getId())
                .origin(route.getOrigin())
                .destination(route.getDestination())
                .distanceKm(route.getDistanceKm())
                .durationMinutes(route.getDurationMinutes())
                .originLocation(route.getOriginLocation())
                .destinationLocation(route.getDestinationLocation())
                .busCount(route.getBuses() == null ? 0 : route.getBuses().size())
                .build();
    }
    public Page<RouteResponse> toPageResponse(Page<BusRoute> routes){
        return routes.map(this::toResponse);
    }

    /** Request DTO → new entity (no id, no buses list). */
    public BusRoute toEntity(RouteRequest request) {
        return BusRoute.builder()
                .origin(request.getOrigin().trim())
                .destination(request.getDestination().trim())
                .distanceKm(request.getDistanceKm())
                .durationMinutes(request.getDurationMinutes())
                .originLocation(request.getOriginLocation())
                .destinationLocation(request.getDestinationLocation())
                .build();
    }

    /** Apply request fields onto an existing entity (used by update / patch). */
    public void updateEntity(BusRoute route, RouteRequest request) {
        if (request.getOrigin()          != null) route.setOrigin(request.getOrigin().trim());
        if (request.getDestination()     != null) route.setDestination(request.getDestination().trim());
        if (request.getDistanceKm()      != null) route.setDistanceKm(request.getDistanceKm());
        if (request.getDurationMinutes() != null) route.setDurationMinutes(request.getDurationMinutes());
        if (request.getOriginLocation()  != null) route.setOriginLocation(request.getOriginLocation());
        if (request.getDestinationLocation() != null) route.setDestinationLocation(request.getDestinationLocation());
    }
}
