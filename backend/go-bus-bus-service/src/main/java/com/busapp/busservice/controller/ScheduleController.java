package com.busapp.busservice.controller;

import com.busapp.busservice.dto.ApiResponse;
import com.busapp.busservice.dto.ScheduleRequest;
import com.busapp.busservice.dto.ScheduleResponse;
import com.busapp.busservice.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // ── Public ────────────────────────────────────────────────────────────────




    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getAllSchedules() {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Schedules retrieved successfully",
                scheduleService.getAllSchedules()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Schedule retrieved successfully",
                scheduleService.getScheduleById(id)));
    }

    @GetMapping("/bus/{busId}")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByBus(
            @PathVariable Long busId) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Schedules retrieved successfully",
                scheduleService.getSchedulesByBus(busId)));
    }

    // ── Admin ─────────────────────────────────────────────────────────────────
    /**
     * Filter schedules by date (ignoring time), route, and price.
     * Date parameters accept date only (e.g., 2026-03-15) and will filter for the entire day.
     * 
     * @param fromDate Start date (inclusive, from 00:00:00)
     * @param toDate End date (inclusive, until 23:59:59)
     * @param maxPrice Maximum price filter
     * @param pageNo Page number (1-indexed)
     * @param pageSize Number of items per page
     * @param routeId Route ID filter
     * @return Paginated schedule results
     */
    @GetMapping("/filter/specification")
    public ResponseEntity<ApiResponse<Page<ScheduleResponse>>>getAllSchedulesWithSpecification(
                                                                                               @RequestParam(value = "fromDate",required = false) 
                                                                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                                               LocalDateTime fromDateTime,
                                                                                               
                                                                                               @RequestParam(value = "toDate",required = false) 
                                                                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                                               LocalDateTime toDateTime,
                                                                                               
                                                                                               @RequestParam(value = "maxPrice",required = false) 
                                                                                               Double maxPrice,
                                                                                               
                                                                                               @RequestParam(value="pageNo",defaultValue = "1") 
                                                                                               Integer pageNo,
                                                                                               
                                                                                               @RequestParam(value = "pageSize",defaultValue = "10") 
                                                                                               Integer pageSize,
                                                                                               
                                                                                               @RequestParam(value = "routeId",required = false) 
                                                                                               Long routeId
    ){
        log.info("=== Controller Received Parameters ===");
        log.info("fromDateTime: {}", fromDateTime);
        log.info("toDateTime: {}", toDateTime);
        log.info("maxPrice: {}", maxPrice);
        log.info("routeId: {}", routeId);
        log.info("pageNo: {}, pageSize: {}", pageNo, pageSize);
        
        // Convert LocalDate to LocalDateTime (start of day and end of day)

        
        log.info("Converted to DateTime - from: {}, to: {}", fromDateTime, toDateTime);
        
        Page<ScheduleResponse> responsePage = scheduleService.getSchedulesWithSpecification(
                routeId, fromDateTime, toDateTime, maxPrice, pageNo, pageSize);
        
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Schedules retrieved successfully", responsePage));
    }

    /**
     * Filter all schedules whose departure falls within [from, to].
     * Example: GET /api/schedules/date-range?from=2026-03-01T00:00:00&to=2026-03-31T23:59:59
     */
//    @GetMapping("/date-range")
//    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByDateRange(
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
//        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Schedules retrieved successfully",
//                scheduleService.getSchedulesByDateRange(from, to)));
//    }
//
//    /**
//     * Filter schedules for a specific bus within a date range.
//     * Example: GET /api/schedules/bus/1/date-range?from=...&to=...
//     */
//    @GetMapping("/bus/{busId}/date-range")
//    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByBusAndDateRange(
//            @PathVariable Long busId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
//        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Schedules retrieved successfully",
//                scheduleService.getSchedulesByBusAndDateRange(busId, from, to)));
//    }
//
//    /**
//     * Filter schedules with a price at or below the given maximum.
//     * Example: GET /api/schedules/price?maxPrice=500
//     */
//    @GetMapping("/price")
//    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByMaxPrice(
//            @RequestParam Double maxPrice) {
//        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Schedules retrieved successfully",
//                scheduleService.getSchedulesByMaxPrice(maxPrice)));
//    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(
            @Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED.value(), "Schedule created successfully",
                        scheduleService.createSchedule(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Schedule updated successfully",
                scheduleService.updateSchedule(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Schedule deleted successfully", null));
    }
}
