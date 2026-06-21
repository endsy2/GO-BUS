package com.busapp.userservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Manages two Redis namespaces:
 *
 *  blacklist:{jti}   → "1"  (TTL = remaining access token lifetime)
 *      Used by gateway to reject revoked access tokens.
 *
 *  refresh:{userId}  → refreshToken (TTL = refresh token lifetime)
 *      Single refresh token per user — issuing a new one overwrites the old.
 */
@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String REFRESH_PREFIX   = "refresh:";

    private final StringRedisTemplate redis;

    // ── Access token blacklist ─────────────────────────────────────────────────

    public void blacklistAccessToken(String token, long ttlMs) {
        redis.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "1",
                ttlMs,
                TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redis.hasKey(BLACKLIST_PREFIX + token));
    }

    // ── Refresh token storage ─────────────────────────────────────────────────

    /**
     * Store refresh token asynchronously to avoid blocking login response.
     * The token is generated and returned immediately, storage happens in background.
     */
    @Async("minioTaskExecutor")
    public void storeRefreshToken(Long userId, String refreshToken, long ttlMs) {
        redis.opsForValue().set(
                REFRESH_PREFIX + userId,
                refreshToken,
                ttlMs,
                TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(Long userId) {
        return redis.opsForValue().get(REFRESH_PREFIX + userId);
    }

    public void deleteRefreshToken(Long userId) {
        redis.delete(REFRESH_PREFIX + userId);
    }
}
