package com.busapp.busservice.service.impl;

import com.busapp.busservice.dto.RouteRequest;
import com.busapp.busservice.dto.RouteResponse;
import com.busapp.busservice.exception.BadRequestException;
import com.busapp.busservice.exception.ResourceNotFoundException;
import com.busapp.busservice.mapper.RouteMapper;
import com.busapp.busservice.model.BusRoute;
import com.busapp.busservice.model.repository.BusRouteRepository;
import com.busapp.busservice.service.RouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final BusRouteRepository routeRepository;
    private final RouteMapper        routeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> getAllRoutes() {
        return routeRepository.findAll()
                .stream()
                .map(routeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RouteResponse> getAllRoutesWithPage(Integer pageNo, Integer pageSize) {
        PageRequest  pageRequest = PageRequest.of(pageNo-1, pageSize);
        return routeMapper.toPageResponse(routeRepository.findAll(pageRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getRouteById(Long id) {
        return routeMapper.toResponse(findRouteOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> searchRoutes(String origin, String destination) {
        String o = (origin      == null || origin.isBlank())      ? null : origin.trim();
        String d = (destination == null || destination.isBlank()) ? null : destination.trim();
        return routeRepository.searchByOriginAndDestination(o, d)
                .stream()
                .map(routeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getRouteByScheduleId(Long scheduleId) {
        BusRoute route = routeRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found for schedule: " + scheduleId));
        return routeMapper.toResponse(route);
    }


    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> getRoutesByScheduleIds(List<Long> scheduleIds) {
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            return List.of();
        }
        List<BusRoute> routes = routeRepository.findByScheduleIds(scheduleIds);
        return routes.stream()
                .map(routeMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<com.busapp.busservice.dto.ScheduleRouteResponse> getRoutesWithScheduleMapping(Set<Long> scheduleIds) {
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            return List.of();
        }
        return routeRepository.findRoutesWithScheduleIds(scheduleIds)
                .stream()
                .map(result -> com.busapp.busservice.dto.ScheduleRouteResponse.builder()
                        .scheduleId((Long) result[0])
                        .route(routeMapper.toResponse((BusRoute) result[1]))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RouteResponse createRoute(RouteRequest request) {
        checkDuplicate(request.getOrigin(), request.getDestination(), null);
        BusRoute route = routeMapper.toEntity(request);
        BusRoute saved = routeRepository.save(route);
        log.info("Route created: id={} [{} -> {}]", saved.getId(), saved.getOrigin(), saved.getDestination());
        return routeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RouteResponse updateRoute(Long id, RouteRequest request) {
        BusRoute route = findRouteOrThrow(id);
        boolean originChanged      = !route.getOrigin().equalsIgnoreCase(request.getOrigin());
        boolean destinationChanged = !route.getDestination().equalsIgnoreCase(request.getDestination());
        if (originChanged || destinationChanged) {
            checkDuplicate(request.getOrigin(), request.getDestination(), id);
        }
        routeMapper.updateEntity(route, request);
        BusRoute saved = routeRepository.save(route);
        log.info("Route updated: id={} [{} -> {}]", saved.getId(), saved.getOrigin(), saved.getDestination());
        return routeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RouteResponse patchRoute(Long id, RouteRequest request) {
        BusRoute route = findRouteOrThrow(id);
        String newOrigin      = request.getOrigin();
        String newDestination = request.getDestination();
        if (newOrigin != null || newDestination != null) {
            String effOrigin = newOrigin      != null ? newOrigin      : route.getOrigin();
            String effDest   = newDestination != null ? newDestination : route.getDestination();
            if (!route.getOrigin().equalsIgnoreCase(effOrigin) || !route.getDestination().equalsIgnoreCase(effDest)) {
                checkDuplicate(effOrigin, effDest, id);
            }
        }
        routeMapper.updateEntity(route, request);
        BusRoute saved = routeRepository.save(route);
        log.info("Route patched: id={} [{} -> {}]", saved.getId(), saved.getOrigin(), saved.getDestination());
        return routeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteRoute(Long id) {
        BusRoute route = findRouteOrThrow(id);
        if (route.getBuses() != null && !route.getBuses().isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete route id=" + id + " — it has " + route.getBuses().size() + " bus(es) assigned.");
        }
        routeRepository.delete(route);
        log.info("Route deleted: id={}", id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BusRoute findRouteOrThrow(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: id=" + id));
    }

    private void checkDuplicate(String origin, String destination, Long excludeId) {
        boolean exists = routeRepository
                .findByOriginIgnoreCaseAndDestinationIgnoreCase(origin.trim(), destination.trim())
                .stream()
                .anyMatch(r -> !r.getId().equals(excludeId));
        if (exists) {
            throw new com.busapp.busservice.exception.DuplicateResourceException(
                    "A route from '" + origin.trim() + "' to '" + destination.trim() + "' already exists.");
        }
    }
}
