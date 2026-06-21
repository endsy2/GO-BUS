package com.busapp.busservice.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LayoutResponse {
    private Long id;
    private String name;
    
    @JsonRawValue
    private String layout;
    
    private String description;
    private LocalDateTime createdAt;
}
