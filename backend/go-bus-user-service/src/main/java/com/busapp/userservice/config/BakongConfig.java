package com.busapp.userservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bakong")
@Getter
@Setter
public class BakongConfig {

    private String accountId;

    private String acquiringBank;

    private String merchantName;


    private String mobileNumber;


    private String storeLabel;


    private String baseUrl;


    private long connectionTimeout=300000;

    private Long pollingIntervalMs=2000L;
}
