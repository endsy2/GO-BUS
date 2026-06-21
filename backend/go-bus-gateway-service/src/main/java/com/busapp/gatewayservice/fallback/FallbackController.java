package com.busapp.gatewayservice.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback endpoints invoked by the CircuitBreaker filter when a downstream
 * service is unavailable or has tripped its circuit.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        return fallbackResponse("user-service");
    }

    @RequestMapping("/bus-service")
    public Mono<ResponseEntity<Map<String, Object>>> busServiceFallback() {
        return fallbackResponse("bus-service");
    }

    @RequestMapping("/booking-service")
    public Mono<ResponseEntity<Map<String, Object>>> bookingServiceFallback() {
        return fallbackResponse("booking-service");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Mono<ResponseEntity<Map<String, Object>>> fallbackResponse(String service) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status",    HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error",     "Service Unavailable",
                "message",   "The " + service + " is currently unavailable. Please try again later.",
                "service",   service
        );
        return Mono.just(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                              .contentType(MediaType.APPLICATION_JSON)
                              .body(body)
        );
    }
}
