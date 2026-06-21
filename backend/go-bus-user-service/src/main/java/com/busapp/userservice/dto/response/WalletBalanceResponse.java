package com.busapp.userservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalletBalanceResponse {
    private double balance;
}
