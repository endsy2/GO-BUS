package com.busapp.busservice.controller;

import com.busapp.busservice.dto.ApiResponse;
import com.busapp.busservice.dto.event.SeatAvailabilityEvent;
import com.busapp.busservice.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test/websocket")
@RequiredArgsConstructor
public class WebSocketTestController {

    private final WebSocketService webSocketService;

    @PostMapping("/seat/{scheduleId}")
    public ResponseEntity<ApiResponse<String>> testSeatEvent(
            @PathVariable Long scheduleId,
            @RequestParam(defaultValue = "SEAT_BOOKED") String type,
            @RequestParam(defaultValue = "1") Long seatId,
            @RequestParam(defaultValue = "A1") String seatNumber
    ) {
        SeatAvailabilityEvent event = SeatAvailabilityEvent.builder()
                .type(type)
                .scheduleId(scheduleId)
                .seatId(seatId)
                .seatNumber(seatNumber)
                .bookingId(type.equals("SEAT_BOOKED") ? 123L : null)
                .status(type.equals("SEAT_BOOKED") ? "BOOKED" : "AVAILABLE")
                .timestamp(LocalDateTime.now())
                .build();

        webSocketService.broadcastSeatAvailability(event);

        return ResponseEntity.ok(ApiResponse.of(
                "Seat event broadcasted",
                "Event sent to /topic/schedule/" + scheduleId + "/seats"
        ));
    }
}
