package com.busapp.userservice.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletAdjustRequest {
    @NotNull
    private Double amount;
    @NotNull
    private String operation; // ADD | DEDUCT
    private String reason;
}
