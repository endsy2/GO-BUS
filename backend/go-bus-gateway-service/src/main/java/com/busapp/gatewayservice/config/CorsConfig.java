package com.busapp.gatewayservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public static UrlBasedCorsConfigurationSource corsConfigurationSource(
            @Value("${cors.allowed-origins:*}") String allowedOrigins,
            @Value("${cors.max-age:3600}") long maxAge) {
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow all origins
        config.addAllowedOriginPattern("*");
        
        // Allow all methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        
        // Allow all headers
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials (required for JWT tokens)
        config.setAllowCredentials(true);
        
        // Max age for preflight requests
        config.setMaxAge(maxAge);
        
        // Expose all headers
        config.addExposedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return source;
    }

    @Bean
    public CorsWebFilter corsWebFilter(UrlBasedCorsConfigurationSource corsConfigurationSource) {
        return new CorsWebFilter(corsConfigurationSource);
    }
}
