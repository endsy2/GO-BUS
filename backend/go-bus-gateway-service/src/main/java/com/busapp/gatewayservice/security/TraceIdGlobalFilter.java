package com.busapp.gatewayservice.security;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * The single source of truth for request tracing.
 *
 * Runs before every other gateway filter. Reuses an inbound {@code X-Trace-Id}
 * (e.g. from a mobile client retry) or mints a fresh UUID, then:
 *  - forwards it to the downstream service on the {@code X-Trace-Id} request header,
 *  - echoes it back to the caller on the response header,
 *  - exposes it on the gateway's own MDC so gateway log lines are correlated too.
 *
 * Downstream services read this header into their MDC (see TraceIdFilter), so a
 * single traceId follows one request across the gateway, user, bus and booking services.
 */
@Slf4j
@Component
public class TraceIdGlobalFilter implements GlobalFilter, Ordered {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    public int getOrder() {
        // Must run before JwtAuthenticationFilter (-100) so every log line — including auth — carries the traceId.
        return -200;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        final String resolvedTraceId = traceId;

        // Build a fresh mutable copy of the headers — never mutate the
        // original ReadOnlyHttpHeaders that Spring wraps the incoming request with.
        HttpHeaders mutableHeaders = new HttpHeaders();
        mutableHeaders.addAll(exchange.getRequest().getHeaders());
        mutableHeaders.set(TRACE_ID_HEADER, resolvedTraceId);
        final HttpHeaders frozenHeaders = HttpHeaders.readOnlyHttpHeaders(mutableHeaders);

        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                return frozenHeaders;
            }
        };

        // Defer response header write until just before the response is committed.
        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().set(TRACE_ID_HEADER, resolvedTraceId);
            return Mono.empty();
        });

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .contextWrite(ctx -> {
                    MDC.put(TRACE_ID_MDC_KEY, resolvedTraceId);
                    return ctx;
                })
                .doFinally(signal -> MDC.remove(TRACE_ID_MDC_KEY));
    }
}
