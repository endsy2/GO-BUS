package com.busapp.busservice.service;

import com.busapp.busservice.dto.RouteRequest;
import com.busapp.busservice.dto.RouteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Set;

public interface RouteService {

    // ── Reads ───────────────────────────────────────────────────────────

    List<RouteResponse> getAllRoutes();

    RouteResponse getRouteById(Long id);

    /**
     * Partial keyword search — either parameter may be null/blank to match any.
     * Example: searchRoutes("Phnom", null) returns all routes originating from Phnom Penh.
     */
    List<RouteResponse> searchRoutes(String origin, String destination);

    /**
     * Get route by schedule ID
     */
    RouteResponse getRouteByScheduleId(Long scheduleId);

    /**
     * Get multiple routes by schedule IDs in a single call (batch operation)
     */
    List<RouteResponse> getRoutesByScheduleIds(List<Long> scheduleIds);

    /**
     * Get routes with schedule ID mapping for batch operations
     */
    List<com.busapp.busservice.dto.ScheduleRouteResponse> getRoutesWithScheduleMapping(Set<Long> scheduleIds);



    // ── Writes ──────────────────────────────────────────────────────────

    RouteResponse createRoute(RouteRequest request);

    /** Full update — all fields replaced from the request body. */
    RouteResponse updateRoute(Long id, RouteRequest request);

    /** Partial update — only non-null fields in the request body are applied. */
    RouteResponse patchRoute(Long id, RouteRequest request);

    void deleteRoute(Long id);

    Page<RouteResponse>getAllRoutesWithPage(Integer pageNo, Integer pageSize);
}
