package com.busapp.busservice.controller;

import com.busapp.busservice.dto.ApiResponse;
import com.busapp.busservice.dto.SeatRequest;
import com.busapp.busservice.dto.SeatResponse;
import com.busapp.busservice.dto.SeatStatusRequest;
import com.busapp.busservice.dto.SeatUpdateRequest;
import com.busapp.busservice.model.enums.SeatStatus;
import com.busapp.busservice.model.enums.SeatType;
import com.busapp.busservice.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // ── Public ────────────────────────────────────────────────────────────────

    @GetMapping("/bus/{busId}")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getSeatsByBus(@PathVariable Long busId) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Seats retrieved successfully",
                seatService.getSeatsByBus(busId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SeatResponse>> getSeatById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Seat retrieved successfully",
                seatService.getSeatById(id)));
    }

    // ── Admin ─────────────────────────────────────────────────────────────────
    /** Filter seats on a bus by seat type (NORMAL, VIP, SLEEPER). */
    @GetMapping("/bus/{busId}/type/{type}")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getSeatsByType(
            @PathVariable Long busId,
            @PathVariable SeatType type) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Seats retrieved successfully",
                seatService.getSeatsByBusAndType(busId, type)));
    }

    /** Look up a single seat by its seat number on a specific bus. */
    @GetMapping("/bus/{busId}/number/{seatNumber}")
    public ResponseEntity<ApiResponse<SeatResponse>> getSeatByNumber(
            @PathVariable Long busId,
            @PathVariable String seatNumber) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Seat retrieved successfully",
                seatService.getSeatByBusAndNumber(busId, seatNumber)));
    }


    @PostMapping
    public ResponseEntity<ApiResponse<SeatResponse>> createSeat(
            @Valid @RequestBody SeatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED.value(), "Seat created successfully", seatService.createSeat(request)));
    }

    /** Bulk-create seats for a bus in one request. */
    @PostMapping("/bus/{busId}/bulk")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> createBulkSeats(
            @PathVariable Long busId,
            @Valid @RequestBody List<SeatRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED.value(), "Seats created successfully",
                        seatService.createBulkSeats(busId, requests)));
    }

    /** Full seat update — number, type, position label, and/or status. */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SeatResponse>> updateSeat(
            @PathVariable Long id,
            @RequestBody SeatUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Seat updated successfully",
                seatService.updateSeat(id, request)));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Seat deleted successfully", null));
    }
}
