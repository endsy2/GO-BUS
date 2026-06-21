package com.busapp.gatewayservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Permission-based authorization filter — runs after JwtAuthenticationFilter (order -100).
 *
 * Rules are evaluated top-to-bottom; the first matching rule wins.
 * Each rule declares ONE required permission — the user must hold that permission
 * in their X-User-Permissions header (comma-separated, forwarded from the JWT claim).
 *
 * Permission catalogue:
 *
 *   USER_READ          — read any user profile (admin)
 *   USER_WRITE         — update any user (admin)
 *   USER_DELETE        — delete any user (admin)
 *   WALLET_READ        — read own wallet & transactions
 *   WALLET_WRITE       — top-up / modify wallet
 *   NOTIFICATION_READ  — read own notifications
 *   NOTIFICATION_WRITE — mark notifications as read
 *   BUS_READ           — read buses, routes, schedules, seats, layouts
 *   BUS_WRITE          — create / update buses, routes, schedules, seats, layouts
 *   BUS_DELETE         — delete buses, routes, schedules, seats, layouts
 *   BOOKING_READ       — read own bookings
 *   BOOKING_WRITE      — create / update bookings
 *   BOOKING_DELETE     — cancel a booking
 *   PAYMENT_READ       — read payments
 *   PAYMENT_WRITE      — initiate a payment
 *   TICKET_READ        — view tickets
 *   PROMO_READ         — read promo codes
 *   PROMO_MANAGE       — create / update / delete promo codes
 *   ADMIN_ACCESS       — unrestricted admin access
 */
@Slf4j
@Component
public class RouteAuthorizationFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /** Sentinel: request must be authenticated, but no specific permission is required. */
    private static final String ANY_AUTHENTICATED = "__AUTHENTICATED__";

    /** Ordered list of authorization rules. First match wins. */
    private static final List<RouteRule> RULES = List.of(

        // ── User Service ─────────────────────────────────────────────────────
        rule("GET",    "/api/users/profile/**",      ANY_AUTHENTICATED),      // own profile
        rule("PUT",    "/api/users/profile/**",      ANY_AUTHENTICATED),      // own profile
        rule("GET",    "/api/users/me/**",           ANY_AUTHENTICATED),      // back-compat
        rule("PUT",    "/api/users/me/**",           ANY_AUTHENTICATED),      // back-compat

        rule("GET",    "/api/users/**",              "USER_READ"),
        rule("PUT",    "/api/users/**",              "USER_WRITE"),
        rule("PATCH",  "/api/users/**",              "USER_WRITE"),
        rule("DELETE", "/api/users/**",              "USER_DELETE"),

        rule("GET",    "/api/users/profile/**",              "USER_READ"),
        rule("PUT",    "/api/users/profile/**",              "USER_WRITE"),
        rule("PATCH",  "/api/users/profile/**",              "USER_WRITE"),
        rule("DELETE", "/api/users/profile/**",              "USER_DELETE"),

        rule("GET",    "/api/wallet/**",             "WALLET_READ"),
        rule("POST",   "/api/wallet/**",             "WALLET_WRITE"),
        rule("PATCH",  "/api/wallet/**",             "WALLET_WRITE"),

        rule("GET",    "/api/notifications/**",      "NOTIFICATION_READ"),
        rule("PUT",    "/api/notifications/**",      "NOTIFICATION_WRITE"),
        rule("PATCH",  "/api/notifications/**",      "NOTIFICATION_WRITE"),

        // ── Bus Service ──────────────────────────────────────────────────────
        rule("GET",    "/api/buses/**",              "BUS_READ"),
        rule("POST",   "/api/buses/**",              "BUS_WRITE"),
        rule("PUT",    "/api/buses/**",              "BUS_WRITE"),
        rule("PATCH",  "/api/buses/**",              "BUS_WRITE"),
        rule("DELETE", "/api/buses/**",              "BUS_DELETE"),

        rule("GET",    "/api/routes/**",             "BUS_READ"),
        rule("POST",   "/api/routes/**",             "BUS_WRITE"),
        rule("PUT",    "/api/routes/**",             "BUS_WRITE"),
        rule("PATCH",  "/api/routes/**",             "BUS_WRITE"),
        rule("DELETE", "/api/routes/**",             "BUS_DELETE"),

        rule("GET",    "/api/schedules/**",          "BUS_READ"),
        rule("POST",   "/api/schedules/**",          "BUS_WRITE"),
        rule("PUT",    "/api/schedules/**",          "BUS_WRITE"),
        rule("PATCH",  "/api/schedules/**",          "BUS_WRITE"),
        rule("DELETE", "/api/schedules/**",          "BUS_DELETE"),

        rule("GET",    "/api/seats/**",              "BUS_READ"),
        rule("POST",   "/api/seats/**",              "BUS_WRITE"),
        rule("PUT",    "/api/seats/**",              "BUS_WRITE"),
        rule("PATCH",  "/api/seats/**",              "BUS_WRITE"),
        rule("DELETE", "/api/seats/**",              "BUS_DELETE"),

        rule("GET",    "/api/layouts/**",            "BUS_READ"),
        rule("POST",   "/api/layouts/**",            "BUS_WRITE"),
        rule("PUT",    "/api/layouts/**",            "BUS_WRITE"),
        rule("PATCH",  "/api/layouts/**",            "BUS_WRITE"),
        rule("DELETE", "/api/layouts/**",            "BUS_DELETE"),

        // ── Booking Service ──────────────────────────────────────────────────
        rule("GET",    "/api/bookings/**",           "BOOKING_READ"),
        rule("POST",   "/api/bookings/**",           "BOOKING_WRITE"),
        rule("PUT",    "/api/bookings/**",           "BOOKING_WRITE"),
        rule("PATCH",  "/api/bookings/**",           "BOOKING_WRITE"),
        rule("DELETE", "/api/bookings/**",           "BOOKING_DELETE"),

        rule("GET","/api/wallets/**",              "WALLET_READ"),

        rule("GET",    "/api/payments/**",           "PAYMENT_READ"),
        rule("POST",   "/api/payments/**",           "PAYMENT_WRITE"),

        rule("GET",    "/api/tickets/**",            "TICKET_READ"),
        rule("POST",   "/api/tickets/**",            "TICKET_READ"),

        rule("GET",    "/api/promos/**",             "PROMO_READ"),
        rule("POST",   "/api/promos/**",             "PROMO_MANAGE"),
        rule("PUT",    "/api/promos/**",             "PROMO_MANAGE"),
        rule("PATCH",  "/api/promos/**",             "PROMO_MANAGE"),
        rule("DELETE", "/api/promos/**",             "PROMO_MANAGE"),

        // ── Admin catch-all ──────────────────────────────────────────────────
        rule("*",      "/api/admin/**",              "ADMIN_ACCESS")
    );

    @Override
    public int getOrder() {
        return -99; // After JwtAuthenticationFilter (-100)
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path   = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        // Find the first matching rule
        RouteRule matched = RULES.stream()
                .filter(r -> methodMatches(r.method(), method)
                          && PATH_MATCHER.match(r.pathPattern(), path))
                .findFirst()
                .orElse(null);

        // No specific rule — allow (already authenticated by JwtAuthenticationFilter)
        if (matched == null) {
            return chain.filter(exchange);
        }

        // ANY_AUTHENTICATED: no permission check needed
        if (ANY_AUTHENTICATED.equals(matched.requiredPermission())) {
            return chain.filter(exchange);
        }

        // Permission check — read from header forwarded by JwtAuthenticationFilter
        String permHeader = exchange.getRequest().getHeaders().getFirst("X-User-Permissions");
        Set<String> userPermissions = permHeader == null || permHeader.isBlank()
                ? Set.of()
                : Arrays.stream(permHeader.split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());

        if (!userPermissions.contains(matched.requiredPermission())) {
            log.warn("Access denied: {} {} — user permissions [{}] missing required [{}]",
                    method, path, permHeader, matched.requiredPermission());
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            exchange.getResponse().getHeaders().add("X-Auth-Error",
                    "Missing permission: " + matched.requiredPermission());
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean methodMatches(String ruleMethod, String requestMethod) {
        return "*".equals(ruleMethod) || ruleMethod.equalsIgnoreCase(requestMethod);
    }

    private static RouteRule rule(String method, String pathPattern, String requiredPermission) {
        return new RouteRule(method, pathPattern, requiredPermission);
    }

    /** Immutable value object for a single authorization rule. */
    private record RouteRule(String method, String pathPattern, String requiredPermission) {}
}
