package com.busapp.bookingservice.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "bakong")
public class BakongConfig {
    private String baseUrl;
    private Long pollingIntervalMs;
    private Long connectionTimeout;
}
