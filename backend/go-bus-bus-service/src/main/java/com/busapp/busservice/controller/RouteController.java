package com.busapp.busservice.controller;

import com.busapp.busservice.dto.ApiResponse;
import com.busapp.busservice.dto.RouteRequest;
import com.busapp.busservice.dto.RouteResponse;
import com.busapp.busservice.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    // ── Public ────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<RouteResponse>>> getAllRoutes() {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Routes retrieved successfully",
                routeService.getAllRoutes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Route retrieved successfully",
                routeService.getRouteById(id)));
    }

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<ApiResponse<RouteResponse>> getRouteByScheduleId(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Route retrieved successfully",
                routeService.getRouteByScheduleId(scheduleId)));
    }

    @PostMapping("/batch/by-schedules")
    public ResponseEntity<ApiResponse<List<RouteResponse>>> getRoutesByScheduleIds(@RequestBody List<Long> scheduleIds) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Routes retrieved successfully",
                routeService.getRoutesByScheduleIds(scheduleIds)));
    }

    @PostMapping("/batch/by-schedules/mapped")
    public ResponseEntity<ApiResponse<List<com.busapp.busservice.dto.ScheduleRouteResponse>>> getRoutesWithScheduleMapping(@RequestBody Set<Long> scheduleIds) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Routes retrieved successfully",
                routeService.getRoutesWithScheduleMapping(scheduleIds)));
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    /**
     * Partial keyword search — either param is optional.
     * GET /api/routes/search?origin=Phnom
     * GET /api/routes/search?origin=Phnom+Penh&destination=Siem+Reap
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RouteResponse>>> searchRoutes(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Routes retrieved successfully",
                routeService.searchRoutes(origin, destination)));
    }
    @GetMapping("/paginate")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<RouteResponse>>>getAllRouteWithPaginate(@RequestParam (value = "pageNo",defaultValue = "1") Integer pageNo,
                                                                                                                   @RequestParam (value = "pageSize",defaultValue = "10") Integer pageSize) {
        Page<RouteResponse> response=routeService.getAllRoutesWithPage(pageNo,pageSize);
        return  ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Routes retrieved successfully",response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RouteResponse>> createRoute(
            @Valid @RequestBody RouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED.value(), "Route created successfully", routeService.createRoute(request)));
    }

    /** Full update — all fields are replaced. Origin and destination are required. */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody RouteRequest request) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Route updated successfully",
                routeService.updateRoute(id, request)));
    }

    /**
     * Partial update — send only the fields you want to change.
     * Fields omitted (null) are kept unchanged.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> patchRoute(
            @PathVariable Long id,
            @RequestBody RouteRequest request) {  // no @Valid — all fields are optional for PATCH
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Route updated successfully",
                routeService.patchRoute(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Route deleted successfully", null));
    }
}
