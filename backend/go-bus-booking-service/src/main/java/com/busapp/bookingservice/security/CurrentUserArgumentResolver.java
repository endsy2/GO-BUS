package com.busapp.bookingservice.security;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves method parameters annotated with @CurrentUser.
 * Extracts the user ID from the X-User-Id header set by the API Gateway.
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) 
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        
        String userIdHeader = webRequest.getHeader("X-User-Id");
        
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            throw new IllegalStateException("User ID not found in request. User must be authenticated.");
        }
        
        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid user ID format: " + userIdHeader);
        }
    }
}
