package com.busapp.userservice.util;

import com.busapp.userservice.exception.ResourceNotFoundException;
import com.busapp.userservice.model.User;
import com.busapp.userservice.repository.UserRepository;
import com.busapp.userservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUtil {
    private final UserRepository userRepository;
    /**
     * Get current username from Spring Security context
     * This is the username (email) from the JWT token
     */
    public String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * Get current user ID from Spring Security context
     * Note: This requires the Authentication principal to contain user ID information
     * If using custom UserDetails, cast the principal to get the ID
     */
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof UserPrincipal user) {
            return user.getUserId();
        }

        return null;
    }

    /**
     * Get current user email from Spring Security context
     * In most JWT implementations, the username IS the email
     */
    public String getCurrentUserEmail() {
        return getCurrentUserName();
    }
    
    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
    public User findUserById(Long userId){
        return userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User not found for userId: " + userId));
    }

}
