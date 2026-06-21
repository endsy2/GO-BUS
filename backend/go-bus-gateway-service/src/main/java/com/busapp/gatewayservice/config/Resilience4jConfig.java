package com.busapp.gatewayservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Configuration
public class Resilience4jConfig {

    // ── Rate Limiter Registry ──────────────────────────────────────────────────

    /**
     * Named rate limiter configs used by {@link com.busapp.gatewayservice.ratelimit.RateLimiterFilter}.
     * Each key in the map becomes a named config that can be looked up via
     * {@code registry.rateLimiter(instanceKey, configName)}.
     *
     * Algorithm: fixed window — {@code limitForPeriod} permits are refreshed every
     * {@code limitRefreshPeriod}. {@code timeoutDuration = 0} means the filter
     * rejects immediately instead of queuing, keeping gateway latency unaffected.
     *
     * Limits:
     *  - auth:    10 req/s per IP    (brute-force guard on login/register)
     *  - default: 30 req/s per user  (general routes)
     *  - booking: 20 req/s per user  (booking/payment — resource-intensive ops)
     */
    @Bean
    public static RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig authConfig = RateLimiterConfig.custom()
                .limitForPeriod(10)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO)
                .build();

        RateLimiterConfig defaultConfig = RateLimiterConfig.custom()
                .limitForPeriod(30)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO)
                .build();

        RateLimiterConfig bookingConfig = RateLimiterConfig.custom()
                .limitForPeriod(20)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO)
                .build();

        return RateLimiterRegistry.of(Map.of(
                "auth",    authConfig,
                "default", defaultConfig,
                "booking", bookingConfig
        ));
    }

    // ── Circuit Breaker Default Configuration ──────────────────────────────────

    /**
     * Default circuit breaker configuration for all services.
     * Individual services can override via application.yml.
     * 
     * Note: Booking service needs longer timeout (300s) for payment processing
     * which is configured in application.yml
     */
    @Bean
    public static Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(10))  // Increased from 5s to 10s
                        .cancelRunningFuture(true)
                        .build())
                .build());
    }
}
