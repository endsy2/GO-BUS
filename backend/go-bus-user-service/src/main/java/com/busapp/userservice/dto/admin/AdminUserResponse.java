package com.busapp.userservice.dto.admin;

import com.busapp.userservice.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String userName;
    private String fullName;
    private String email;
    private String phone;
    private String image;
    private Gender gender;
    private String googleId;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Wallet info
    private Double walletBalance;
    private String walletStatus;
    private String walletCurrency;

    // Roles
    private List<String> roles;
    private List<String> permissions;

}
