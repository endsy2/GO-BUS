package com.busapp.busservice.controller;

import com.busapp.busservice.dto.ApiResponse;
import com.busapp.busservice.dto.BusDetailResponse;
import com.busapp.busservice.dto.BusRequest;
import com.busapp.busservice.dto.BusResponse;
import com.busapp.busservice.model.enums.BusStatus;
import com.busapp.busservice.model.enums.BusType;
import com.busapp.busservice.service.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
@Slf4j
public class BusController {

    private final BusService busService;

    // ── Public ────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<BusResponse>>> getAllBuses() {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Buses retrieved successfully",
                busService.getAllBuses()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BusDetailResponse>> getBusById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Bus retrieved successfully",
                busService.getBusById(id)));
    }

//    @GetMapping("/schedule/{scheduleId}")
//    public ResponseEntity<ApiResponse<BusDetailResponse>> getBusByScheduleId(@PathVariable Long scheduleId) {
//        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Bus retrieved successfully",
//                busService.getBusByScheduleId(scheduleId)));
//    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<ApiResponse<List<BusResponse>>> getBusesByRoute(
            @PathVariable Long routeId) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Buses retrieved successfully",
                busService.getBusesByRoute(routeId)));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<BusResponse>>> filterBuses(
            @RequestParam(required = false,defaultValue = "1") Integer pageNo,
            @RequestParam(required = false,defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) BusType busType,
            @RequestParam(required = false) BusStatus status,
            @RequestParam(required = false) String busNumber,
            @RequestParam(required = false) String plate,
            @RequestParam(required = false) Integer minSeats,
            @RequestParam(required = false) Integer maxSeats) {
        
        log.debug("[BUS_CONTROLLER] Filter buses - status={}, busType={}, routeId={}", status, busType, routeId);

        ApiResponse<Page<BusResponse>>response=ApiResponse.of(HttpStatus.OK.value(), "Buses retrieved successfully",
                busService.filterBuses(pageNo,pageSize,routeId, busType, status, busNumber, plate, minSeats, maxSeats));
        return ResponseEntity.ok(response);
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    /** Find a bus by its unique fleet number, e.g. "BUS-001". */
    @GetMapping("/number/{busNumber}")
    public ResponseEntity<ApiResponse<BusResponse>> getBusByNumber(
            @PathVariable String busNumber) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Bus retrieved successfully",
                busService.getBusByNumber(busNumber)));
    }

    /** Filter buses on a route by bus type (AC, NON_AC, SLEEPER, SEATER). */
    @GetMapping("/route/{routeId}/type/{type}")
    public ResponseEntity<ApiResponse<List<BusResponse>>> getBusesByRouteAndType(
            @PathVariable Long routeId,
            @PathVariable BusType type) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Buses retrieved successfully",
                busService.getBusesByRouteAndType(routeId, type)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BusResponse>> createBus(
            @Valid @RequestBody BusRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED.value(), "Bus created successfully", busService.createBus(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BusResponse>> updateBus(
            @PathVariable Long id,
            @Valid @RequestBody BusRequest request) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Bus updated successfully",
                busService.updateBus(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBus(@PathVariable Long id) {
        busService.deleteBus(id);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Bus deleted successfully", null));
    }
    
    /**
     * Get count of active buses for dashboard statistics
     */
    @GetMapping("/count/active")
    public ResponseEntity<ApiResponse<Integer>> getActiveBusCount() {
        Integer count = busService.getActiveBusCount();
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Active bus count retrieved successfully", count));
    }
    
    /**
     * Get seat types for multiple seats (batch endpoint)
     * Used by booking-service for velocity analysis
     */
    @PostMapping("/seats/batch/types")
    public ResponseEntity<ApiResponse<java.util.Map<Long, String>>> getSeatTypesBatch(
            @RequestBody java.util.Set<Long> seatIds) {
        java.util.Map<Long, String> seatTypes = busService.getSeatTypesBatch(seatIds);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Seat types retrieved successfully", seatTypes));
    }
}
