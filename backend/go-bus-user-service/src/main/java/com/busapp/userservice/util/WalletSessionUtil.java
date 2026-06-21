package com.busapp.userservice.util;

import com.busapp.userservice.exception.WalletNotAuthenticatedException;
import com.busapp.userservice.service.WalletSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility class for wallet session validation
 * Provides reusable method to check and refresh wallet sessions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WalletSessionUtil {

    private final WalletSessionService walletSessionService;
    private final UserUtil userUtil;

    /**
     * Validate wallet session token and refresh if valid
     * 
     * @param walletSessionToken Session token from X-Wallet-Session header
     * @throws WalletNotAuthenticatedException if session is invalid or expired
     */
    public void validateAndRefreshSession(String walletSessionToken) {
        Long currentUserId = userUtil.getCurrentUserId();
        
        if (!walletSessionService.isWalletSessionValid(currentUserId, walletSessionToken)) {
            log.error("[WALLET AUTH] Invalid wallet session for user: {}", currentUserId);
            throw new WalletNotAuthenticatedException(
                    "Wallet session invalid or expired. Please login to your wallet first.");
        }
        
        // Refresh session on each valid request
        walletSessionService.refreshWalletSession(currentUserId);
        log.debug("[WALLET AUTH] Session validated and refreshed for user: {}", currentUserId);
    }

    /**
     * Check if wallet session is valid without throwing exception
     * 
     * @param walletSessionToken Session token to check
     * @return true if valid, false otherwise
     */
    public boolean isSessionValid(String walletSessionToken) {
        if (walletSessionToken == null || walletSessionToken.isBlank()) {
            return false;
        }
        
        Long currentUserId = userUtil.getCurrentUserId();
        return walletSessionService.isWalletSessionValid(currentUserId, walletSessionToken);
    }
}
