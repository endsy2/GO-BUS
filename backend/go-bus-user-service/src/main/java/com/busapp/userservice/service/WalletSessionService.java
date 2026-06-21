package com.busapp.userservice.service;

public interface WalletSessionService {
    
    /**
     * Create wallet session after successful PIN login
     * @param userId User ID
     * @return Session token
     */
    String createWalletSession(Long userId);
    
    /**
     * Validate wallet session token
     * @param userId User ID
     * @param sessionToken Session token
     * @return true if valid, false otherwise
     */
    boolean isWalletSessionValid(Long userId, String sessionToken);
    
    /**
     * Invalidate wallet session (logout)
     * @param userId User ID
     */
    void invalidateWalletSession(Long userId);
    
    /**
     * Refresh wallet session (extend expiry)
     * @param userId User ID
     */
    void refreshWalletSession(Long userId);
}
