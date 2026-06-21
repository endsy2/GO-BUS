package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.dto.event.DashboardUpdateEvent;
import com.busapp.bookingservice.dto.event.PaymentStatusEvent;
import com.busapp.bookingservice.dto.response.ApiResponse;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import com.busapp.bookingservice.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test/websocket")
@RequiredArgsConstructor
public class WebSocketTestController {

    private final WebSocketService webSocketService;

    @PostMapping("/payment/{userId}")
    public ResponseEntity<ApiResponse<String>> testPaymentEvent(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "PAYMENT_SUCCESS") String type,
            @RequestParam(defaultValue = "1") Long paymentId,
            @RequestParam(defaultValue = "1") Long bookingId
    ) {
        PaymentStatusEvent event = PaymentStatusEvent.builder()
                .type(type)
                .paymentId(paymentId)
                .bookingId(bookingId)
                .userId(userId)
                .status(PaymentStatus.SUCCESS)
                .amount(25.50)
                .timestamp(LocalDateTime.now())
                .build();

        webSocketService.broadcastPaymentStatusToUser(userId, event);

        return ResponseEntity.ok(ApiResponse.of(
                "Payment event broadcasted",
                "Event sent to user: " + userId
        ));
    }

    @PostMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> testDashboardEvent(
            @RequestParam(defaultValue = "DASHBOARD_UPDATE") String type
    ) {
        DashboardUpdateEvent event = DashboardUpdateEvent.builder()
                .type(type)
                .activeBookings(150L)
                .todayRevenue(BigDecimal.valueOf(12850.00))
                .pendingRefunds(6L)
                .todayBookings(45L)
                .timestamp(LocalDateTime.now())
                .build();

        webSocketService.broadcastDashboardUpdate(event);

        return ResponseEntity.ok(ApiResponse.of(
                "Dashboard event broadcasted",
                "Event sent to /topic/admin/dashboard"
        ));
    }
}
