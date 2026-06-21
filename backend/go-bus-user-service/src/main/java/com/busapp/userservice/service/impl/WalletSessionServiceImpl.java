package com.busapp.userservice.service.impl;

import com.busapp.userservice.service.WalletSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletSessionServiceImpl implements WalletSessionService {

    private final StringRedisTemplate stringRedisTemplate;
    
    private static final String WALLET_SESSION_PREFIX = "wallet:session:";
    private static final long SESSION_EXPIRY_MINUTES = 30; // 30 minutes

    @Override
    public String createWalletSession(Long userId) {
        String sessionToken = UUID.randomUUID().toString();
        String key = WALLET_SESSION_PREFIX + userId;
        
        stringRedisTemplate.opsForValue().set(
            key, 
            sessionToken, 
            SESSION_EXPIRY_MINUTES, 
            TimeUnit.MINUTES
        );
        
        log.info("[WALLET SESSION] Created session for user: {}", userId);
        return sessionToken;
    }

    @Override
    public boolean isWalletSessionValid(Long userId, String sessionToken) {
        if (sessionToken == null || sessionToken.isBlank()) {
            return false;
        }
        
        String key = WALLET_SESSION_PREFIX + userId;
        String storedToken = stringRedisTemplate.opsForValue().get(key);
        
        boolean isValid = sessionToken.equals(storedToken);
        
        if (isValid) {
            log.debug("[WALLET SESSION] Valid session for user: {}", userId);
        } else {
            log.warn("[WALLET SESSION] Invalid or expired session for user: {}", userId);
        }
        
        return isValid;
    }

    @Override
    public void invalidateWalletSession(Long userId) {
        String key = WALLET_SESSION_PREFIX + userId;
        stringRedisTemplate.delete(key);
        log.info("[WALLET SESSION] Invalidated session for user: {}", userId);
    }

    @Override
    public void refreshWalletSession(Long userId) {
        String key = WALLET_SESSION_PREFIX + userId;
        stringRedisTemplate.expire(key, SESSION_EXPIRY_MINUTES, TimeUnit.MINUTES);
        log.debug("[WALLET SESSION] Refreshed session for user: {}", userId);
    }
}
