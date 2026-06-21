package com.busapp.bookingservice.controller;

import com.busapp.bookingservice.dto.response.ApiResponse;
import com.busapp.bookingservice.service.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Test controller for RabbitMQ notification system
 * Remove this in production or secure with admin role
 */
@RestController
@RequestMapping("/api/v1/test/notifications")
@RequiredArgsConstructor
public class NotificationTestController {

    private final NotificationPublisher notificationPublisher;

    @PostMapping("/custom")
    public ResponseEntity<ApiResponse<String>> sendCustomNotification(
            @RequestParam String message) {
        
        notificationPublisher.publishCustomAlert(message);
        
        return ResponseEntity.ok(ApiResponse.of(
                "Notification published to RabbitMQ successfully",
                "Event sent to queue"
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.of(
                "Notification system is running",
                "RabbitMQ integration active"
        ));
    }
}
