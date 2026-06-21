package com.busapp.busservice.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class UserUtil {
    
    /**
     * Get current user ID from X-User-Id header (set by gateway)
     */
    public Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        String userId = request.getHeader("X-User-Id");
        return userId != null ? Long.parseLong(userId) : null;
    }

    /**
     * Get current user email from X-User-Email header
     */
    public String getCurrentUserEmail() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader("X-User-Email");
    }

    /**
     * Get current username from X-User-Name header
     */
    public String getCurrentUserName() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader("X-User-Name");
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
