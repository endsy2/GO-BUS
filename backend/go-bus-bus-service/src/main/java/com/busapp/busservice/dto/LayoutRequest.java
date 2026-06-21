package com.busapp.busservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LayoutRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String layout; // JSON string representing seat layout
    private String description;
}
