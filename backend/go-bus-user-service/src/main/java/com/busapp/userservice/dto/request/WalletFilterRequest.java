package com.busapp.userservice.dto.request;

import com.busapp.userservice.model.enums.WalletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletFilterRequest {
    private Long userId;
    private String name;
    private WalletStatus status;
    private Double minBalance;
    private Double maxBalance;
    private String currency;
}
