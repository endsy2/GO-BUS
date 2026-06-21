package com.busapp.userservice.security;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class UserPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(UserPrincipal.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter,
                                   ModelAndViewContainer mavContainer,
                                   @NonNull NativeWebRequest webRequest,
                                   WebDataBinderFactory binderFactory) {
        String userId = webRequest.getHeader("X-User-Id");
        String email = webRequest.getHeader("X-User-Email");
        String userName = webRequest.getHeader("X-User-Name");
        String rolesHeader = webRequest.getHeader("X-User-Roles");
        String permissionsHeader = webRequest.getHeader("X-User-Permissions");

        List<String> roles = parseList(rolesHeader);
        List<String> permissions = parseList(permissionsHeader);

        return UserPrincipal.builder()
                .userId(userId != null ? Long.parseLong(userId) : null)
                .email(email)
                .userName(userName)
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    private List<String> parseList(String header) {
        if (header == null || header.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(header.split(","));
    }
}
