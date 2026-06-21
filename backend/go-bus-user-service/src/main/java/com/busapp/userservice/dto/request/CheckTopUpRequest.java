package com.busapp.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckTopUpRequest {
    
    @NotBlank(message = "Transaction hash (md5) is required")
    private String hash;
}
