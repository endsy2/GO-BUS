package com.busapp.gatewayservice.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Key resolvers for the Resilience4j-backed rate limiter.
 *
 *  - ipKeyResolver:   resolves by client IP  → used for public/auth routes
 *  - userKeyResolver: resolves by X-User-Id header, falls back to IP
 *                     → used for all authenticated routes
 *
 * Rate limiter limits are configured in {@link com.busapp.gatewayservice.config.Resilience4jConfig}.
 */
@Configuration
public class RateLimiterConfig {

    // ── Key Resolvers ──────────────────────────────────────────────────────────

    /**
     * Primary resolver – uses X-User-Id injected by JwtAuthenticationFilter.
     * Falls back to client IP for unauthenticated traffic.
     */
    @Bean
    @Primary
    public static KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                return Mono.just("user:" + userId);
            }
            return Mono.just("ip:" + resolveClientIp(exchange));
        };
    }

    /**
     * IP-only resolver – used for auth routes where no JWT is expected.
     */
    @Bean
    public static KeyResolver ipKeyResolver() {
        return exchange -> Mono.just("ip:" + resolveClientIp(exchange));
    }

    /**
     * Resolve the client IP.
     *
     * ⚠️ TEST-ONLY: this trusts the X-Forwarded-For header so a load tool can
     * simulate many distinct client IPs from a single machine. X-Forwarded-For is
     * spoofable, so in production the per-IP rate limit could be trivially
     * bypassed. REMOVE the header branch (keep only resolveIp on the socket
     * address) before deploying.
     */
    private static String resolveClientIp(org.springframework.web.server.ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return resolveIp(exchange.getRequest().getRemoteAddress());
    }

    private static String resolveIp(InetSocketAddress remote) {
        return Optional.ofNullable(remote)
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::getHostAddress)
                .orElse("unknown");
    }
}
