package com.busapp.gatewayservice.ratelimit;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Resilience4j-backed rate limiter for Spring Cloud Gateway.
 *
 * For each incoming request the filter:
 *  1. Resolves a key (user ID or client IP).
 *  2. Picks a named rate-limiter config based on the request path.
 *  3. Looks up (or lazily creates) a per-key {@link RateLimiter} instance
 *     from the {@link RateLimiterRegistry}.
 *  4. Calls {@link RateLimiter#tryAcquirePermission()} — non-blocking,
 *     returns immediately because {@code timeoutDuration = 0}.
 *  5. Returns {@code 429 Too Many Requests} if no permit is available,
 *     otherwise forwards the request down the filter chain.
 *
 * Named configs (defined in Resilience4jConfig):
 *  - "auth"    → 10 permits/s per IP  (login/register routes)
 *  - "booking" → 20 permits/s per user (booking/payment routes)
 *  - "default" → 30 permits/s per user (all other routes)
 */
@Slf4j
@Component
public class RateLimiterFilter implements GlobalFilter, Ordered {

    private static final String RATE_LIMIT_JSON =
            "{\"status\":429,\"error\":\"Too Many Requests\"," +
            "\"message\":\"Rate limit exceeded. Please slow down and retry after 1 second.\"}";

    private final RateLimiterRegistry rateLimiterRegistry;
    private final KeyResolver userKeyResolver;
    private final KeyResolver ipKeyResolver;

    public RateLimiterFilter(
            RateLimiterRegistry rateLimiterRegistry,
            @Qualifier("userKeyResolver") KeyResolver userKeyResolver,
            @Qualifier("ipKeyResolver")   KeyResolver ipKeyResolver) {
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.userKeyResolver     = userKeyResolver;
        this.ipKeyResolver       = ipKeyResolver;
    }

    @Override
    public int getOrder() {
        // Run before CircuitBreaker (HIGHEST_PRECEDENCE + 1) so rate-limited
        // requests never touch the downstream circuit breaker counters.
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path       = exchange.getRequest().getURI().getPath();
        String configName = resolveConfigName(path);
        boolean isAuth    = path.startsWith("/api/auth/");

        KeyResolver resolver = isAuth ? ipKeyResolver : userKeyResolver;

        return resolver.resolve(exchange).flatMap(key -> {
            // Instance key = "<config>:<user/ip>" — one RateLimiter per (config, identity)
            String instanceKey = configName + ":" + key;
            RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(instanceKey, configName);

            // reservePermission() returns nanos-to-wait, or -1 if no permit
            // available within timeoutDuration. Since timeoutDuration=0 this
            // is always non-blocking: 0 means permit granted, -1 means denied.
            if (rateLimiter.reservePermission() >= 0) {
                return chain.filter(exchange);
            }

            log.warn("Rate limit exceeded — config={} path={} key={}", configName, path, key);
            return rejectWithTooManyRequests(exchange);
        });
    }

    /**
     * Maps a request path to the rate-limiter config name.
     */
    private String resolveConfigName(String path) {
        if (path.startsWith("/api/auth/")) {
            return "auth";
        }
        if (path.startsWith("/api/bookings/")
                || path.startsWith("/api/payments/")
                || path.startsWith("/api/tickets/")
                || path.startsWith("/api/promos/")
                || path.startsWith("/api/admin/bookings/")) {
            return "booking";
        }
        return "default";
    }

    private Mono<Void> rejectWithTooManyRequests(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(HttpHeaders.RETRY_AFTER, "1");
        byte[] bytes = RATE_LIMIT_JSON.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
