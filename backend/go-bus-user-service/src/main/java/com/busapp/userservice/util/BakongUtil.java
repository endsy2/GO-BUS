package com.busapp.userservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class BakongUtil {
    private static final String REDIS_TOKEN_KEY = "bakong:token";
    private static final String REDIS_EXPIRY_KEY = "bakong:token:expiry";

    @Value("${bakong.base-url}")
    private String baseUrl;

    @Value("${bakong.email}")
    private String email;

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final StringRedisTemplate stringRedisTemplate;

    // Fallback in-memory cache if Redis is unavailable
    private volatile String memoryToken;
    private volatile Instant memoryExpiry;

    public synchronized String getToken() {
        // Try Redis first
        try {
            String cachedToken = stringRedisTemplate.opsForValue().get(REDIS_TOKEN_KEY);
            String expiryStr = stringRedisTemplate.opsForValue().get(REDIS_EXPIRY_KEY);

            if (cachedToken != null && expiryStr != null) {
                Instant tokenExpiry = Instant.parse(expiryStr);
                if (Instant.now().isBefore(tokenExpiry)) {
                    log.debug("Using cached Bakong token from Redis");
                    return cachedToken;
                }
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, falling back to in-memory cache: {}", e.getMessage());
            // Fall through to check memory cache
        }

        // Fallback to in-memory cache
        if (memoryToken != null && memoryExpiry != null && Instant.now().isBefore(memoryExpiry)) {
            log.debug("Using cached Bakong token from memory");
            return memoryToken;
        }

        // No valid cache, fetch new token
        return fetchNewToken();
    }

    private String fetchNewToken() {
        log.info("Requesting new token from Bakong API");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity =
                new HttpEntity<>(Map.of("email", email), headers);

        String url = baseUrl.replaceAll("/+$", "") + "/v1/renew_token";

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        try {
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode tokenNode = root.path("data").path("token");

            if (tokenNode.isMissingNode() || tokenNode.isNull()) {
                throw new RuntimeException("Bakong token not returned from API");
            }

            String newToken = tokenNode.asText();

            // Decode JWT payload to get expiry
            String[] parts = newToken.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            JsonNode payloadNode = mapper.readTree(payload);
            long exp = payloadNode.path("exp").asLong();

            Instant tokenExpiry = Instant.ofEpochSecond(exp);

            // Store in memory first (always works)
            memoryToken = newToken;
            memoryExpiry = tokenExpiry;
            log.info("Bakong token cached in memory. Expires at: {}", tokenExpiry);

            // Try to store in Redis (best effort)
            try {
                long ttlSeconds = Duration.between(Instant.now(), tokenExpiry).getSeconds() - 60;
                if (ttlSeconds > 0) {
                    stringRedisTemplate.opsForValue().set(REDIS_TOKEN_KEY, newToken, ttlSeconds, TimeUnit.SECONDS);
                    stringRedisTemplate.opsForValue().set(REDIS_EXPIRY_KEY, tokenExpiry.toString(), ttlSeconds, TimeUnit.SECONDS);
                    log.info("Bakong token also cached in Redis");
                }
            } catch (Exception e) {
                log.warn("Failed to cache token in Redis (using memory cache only): {}", e.getMessage());
            }

            return newToken;

        } catch (Exception e) {
            log.error("Failed to parse Bakong token", e);
            throw new RuntimeException("Failed to obtain Bakong token", e);
        }
    }
}
