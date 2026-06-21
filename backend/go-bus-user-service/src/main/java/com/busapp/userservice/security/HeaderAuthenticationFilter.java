package com.busapp.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter that extracts user information from headers set by the API Gateway
 * and populates the Spring Security context.
 * 
 * The gateway validates JWT tokens and forwards user info via headers:
 * - X-User-Id: User ID
 * - X-User-Email: User email
 * - X-User-Name: Username
 * - X-User-Roles: Comma-separated roles
 * - X-User-Permissions: Comma-separated permissions
 */
@Slf4j
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String userId = request.getHeader("X-User-Id");
        String email = request.getHeader("X-User-Email");
        String userName = request.getHeader("X-User-Name");
        String rolesHeader = request.getHeader("X-User-Roles");
        String permissionsHeader = request.getHeader("X-User-Permissions");

        // Only set authentication if user ID is present (meaning request came through gateway with valid JWT)
        if (userId != null && !userId.isBlank()) {
            try {
                List<String> roles = parseList(rolesHeader);
                List<String> permissions = parseList(permissionsHeader);

                // Create UserPrincipal with all user info
                UserPrincipal userPrincipal = UserPrincipal.builder()
                        .userId(Long.parseLong(userId))
                        .email(email)
                        .userName(userName)
                        .roles(roles)
                        .permissions(permissions)
                        .build();

                // Create authorities from roles and permissions
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
                
                permissions.stream()
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);

                // Create authentication token with UserPrincipal as principal
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal,  // principal
                                null,           // credentials (not needed)
                                authorities     // authorities
                        );

                // Set in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("Set authentication for user: {} (ID: {})", userName, userId);
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID format in header: {}", userId);
            }
        }

        filterChain.doFilter(request, response);
    }

    private List<String> parseList(String header) {
        if (header == null || header.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(header.split(","));
    }
}
