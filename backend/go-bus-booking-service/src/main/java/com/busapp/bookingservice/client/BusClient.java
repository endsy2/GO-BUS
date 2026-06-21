package com.busapp.bookingservice.client;

import com.busapp.bookingservice.dto.request.SeatStatusRequest;
import com.busapp.bookingservice.model.enums.SeatStatus;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Feign client to communicate with bus-service via Eureka load-balancer.
 */
@FeignClient(name = "bus-service",
        url = "${external.bus-service.url}",
        path = "${external.bus-service.path}"
)
public interface BusClient {

    @GetMapping("/buses/{id}")
    ResponseEntity<ApiResponse<BusInfo>> getBusById(@PathVariable("id") Long id);

    @GetMapping("/schedules/{id}")
    ResponseEntity<ApiResponse<ScheduleInfo>> getScheduleById(@PathVariable("id") Long id);

    @GetMapping("/routes/{id}")
    ResponseEntity<ApiResponse<RouteInfo>> getRouteById(@PathVariable("id") Long id);

    @GetMapping("/routes/schedule/{scheduleId}")
    ResponseEntity<ApiResponse<RouteInfo>> getRouteByScheduleId(@PathVariable("scheduleId") Long id);

    @PostMapping("/routes/batch/by-schedules")
    ResponseEntity<ApiResponse<java.util.List<RouteInfo>>> getRoutesByScheduleIds(@RequestBody java.util.List<Long> scheduleIds);

    @PostMapping("/routes/batch/by-schedules/mapped")
    ResponseEntity<ApiResponse<java.util.List<ScheduleRouteInfo>>> getRoutesWithScheduleMapping(@RequestBody Set<Long> scheduleIds);

    @PutMapping("/seats/{id}/status")
    ResponseEntity<ApiResponse<SeatResponse>> updateSeatStatus(@PathVariable Long id,
                                                               @Valid @RequestBody SeatStatusRequest request);
    @GetMapping("/seats/{id}")
    ResponseEntity<ApiResponse<SeatResponse>> getSeatById(@PathVariable Long id);

    // ── Schedule Seat Endpoints ────────────────────────────────────────────────
    
    @GetMapping("/schedule-seats/schedule/{scheduleId}/available")
    ResponseEntity<ApiResponse<List<ScheduleSeatResponse>>> getAvailableSeatsBySchedule(
            @PathVariable("scheduleId") Long scheduleId);
    
    @PostMapping("/schedule-seats/schedule/{scheduleId}/check-availability")
    ResponseEntity<ApiResponse<Boolean>> checkSeatsAvailability(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestBody List<Long> seatIds);
    
    @PostMapping("/schedule-seats/schedule/{scheduleId}/book")
    ResponseEntity<ApiResponse<Void>> bookSeats(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("bookingId") Long bookingId,
            @RequestBody List<Long> seatIds);
    
    @PostMapping("/schedule-seats/booking/{bookingId}/release")
    ResponseEntity<ApiResponse<Void>> releaseSeats(@PathVariable("bookingId") Long bookingId);
    
    @GetMapping("/buses/count/active")
    ResponseEntity<ApiResponse<Integer>> getActiveBusCount();
    
    // Endpoint lives in BusController (base path /api/buses), so the full path
    // is /api/buses/seats/batch/types — not /api/seats/batch/types.
    @PostMapping("/buses/seats/batch/types")
    ResponseEntity<ApiResponse<Map<Long, String>>> getSeatTypesBatch(@RequestBody Set<Long> seatIds);




    record ApiResponse<T>(
            String endpoint,
            String message,
            T data
    ) {}

    record BusInfo(
            Long   id,
            String busNumber,
            String busType,
            RouteResponse   route,
            int    totalSeats
    ) {}

    record RouteResponse(
            Long   id,
            String origin,
            String destination,
            Double  distanceKm,
            Integer durationMinutes,
            String  location,
            /** Number of buses currently assigned to this route. */
            int     busCount
    ){}

    record ScheduleInfo(
            Long   id,
            Long   busId,
            double price,
            LocalDateTime departureDateTime,
            LocalDateTime arrivalDateTime
    ) {}
    
    record RouteInfo(
            Long   id,
            String origin,
            String destination,
            String name,
            double distance,
            double duration
    ) {}
    
    record SeatResponse(
            Long id,
            Long busId,
            String seatNumber,
            String seatType,
            String positionLabel
    ){}

    record ScheduleRouteInfo(
            Long scheduleId,
            RouteInfo route
    ){}
    
    record ScheduleSeatResponse(
            Long id,
            Long scheduleId,
            Long seatId,
            String seatNumber,
            String seatType,
            SeatStatus status,
            Long bookingId
    ){}
}
