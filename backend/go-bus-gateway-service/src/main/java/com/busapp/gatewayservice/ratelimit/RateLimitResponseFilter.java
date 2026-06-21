package com.busapp.gatewayservice.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Intercepts 429 TOO_MANY_REQUESTS responses produced by the RequestRateLimiter
 * gateway filter and replaces the empty body with a structured JSON error message.
 *
 * Also adds a Retry-After: 1 header so clients know they may retry after 1 second.
 */
@Slf4j
@Component
public class RateLimitResponseFilter implements GlobalFilter, Ordered {

    private static final String RATE_LIMIT_JSON =
            "{\"status\":429,\"error\":\"Too Many Requests\"," +
            "\"message\":\"Rate limit exceeded. Please slow down and retry after 1 second.\"}";

    @Override
    public int getOrder() {
        // Run after the rate-limiter filter (which is Ordered.HIGHEST_PRECEDENCE + 3)
        // but before the response is committed to the client.
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.defer(() -> {
            ServerHttpResponse response = exchange.getResponse();

            if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                String path = exchange.getRequest().getURI().getPath();
                String key  = exchange.getRequest().getHeaders().getFirst("X-User-Id");
                log.warn("Rate limit exceeded — path={} key={}", path, key != null ? key : "IP");

                // Write a proper JSON body instead of an empty 429.
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                response.getHeaders().set(HttpHeaders.RETRY_AFTER, "1");

                byte[] bytes = RATE_LIMIT_JSON.getBytes(StandardCharsets.UTF_8);
                DataBuffer buffer = response.bufferFactory().wrap(bytes);
                return response.writeWith(Mono.just(buffer));
            }
            return Mono.empty();
        }));
    }
}
