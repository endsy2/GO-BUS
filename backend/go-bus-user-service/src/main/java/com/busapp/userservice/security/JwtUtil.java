package com.busapp.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final KeyPair rsaKeyPair;

    /** Access token TTL in milliseconds (default 15 min) */
    @Value("${jwt.access-token-expiry:900000}")
    private long accessTokenExpiry;

    /** Refresh token TTL in milliseconds (default 7 days) */
    @Value("${jwt.refresh-token-expiry:604800000}")
    private long refreshTokenExpiry;

    // ── Token Generation ──────────────────────────────────────────────────────

    public String generateAccessToken(Long userId, String email, String userName,
                                        List<String> roles, List<String> permissions) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email",       email)
                .claim("userName",    userName)
                .claim("type",        "access")
                .claim("roles",       roles)
                .claim("permissions", permissions)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(rsaKeyPair.getPrivate())
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiry))
                .signWith(rsaKeyPair.getPrivate())
                .compact();
    }

    // ── Token Parsing ─────────────────────────────────────────────────────────

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(rsaKeyPair.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Remaining lifetime of a token in milliseconds. */
    public long getRemainingTtlMs(String token) {
        Date expiry = parseToken(token).getExpiration();
        long remaining = expiry.getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public long getAccessTokenExpiry()  { return accessTokenExpiry;  }
    public long getRefreshTokenExpiry() { return refreshTokenExpiry; }
}
