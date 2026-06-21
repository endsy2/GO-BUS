package com.busapp.userservice.controller;

import com.busapp.userservice.exception.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Base class for controllers that need to resolve the authenticated user's ID.
 *
 * The gateway validates the JWT and forwards the identity as an {@code X-User-Id}
 * header to every downstream service. Any controller that needs the calling
 * user's ID should extend this class and call {@link #getCurrentUserId(HttpServletRequest)}.
 */
public abstract class BaseController {

    protected static final String USER_ID_HEADER = "X-User-Id";

    /**
     * Reads the {@code X-User-Id} header injected by the gateway filter and
     * returns it as a {@code Long}. Throws {@link BadRequestException} if the
     * header is absent or cannot be parsed (should never happen in normal flow).
     */
    protected Long getCurrentUserId(HttpServletRequest request) {
        String value = request.getHeader(USER_ID_HEADER);
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Missing X-User-Id header — request must pass through the gateway.");
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid X-User-Id header value: " + value);
        }
    }
}
