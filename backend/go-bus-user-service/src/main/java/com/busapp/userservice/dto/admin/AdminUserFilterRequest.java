package com.busapp.userservice.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserFilterRequest {
    private String search;       // matches userName, fullName, email, phone
    private String walletStatus; // ACTIVE | FROZEN | CLOSED
    private Boolean active;
    private String fromDate;     // ISO date string
    private String toDate;
    private Boolean isEmployee;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 20;
}
