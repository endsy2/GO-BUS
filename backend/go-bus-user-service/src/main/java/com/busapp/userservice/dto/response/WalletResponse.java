package com.busapp.userservice.dto.response;

import com.busapp.userservice.model.enums.Currency;
import com.busapp.userservice.model.enums.Gender;
import com.busapp.userservice.model.enums.WalletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private UUID          id;
    private Long          userId;
    private String        userName;
    private String        fullName;
    private Double        balance;
    private Currency      currency;
    private WalletStatus  status;
    private LocalDateTime lastTransaction;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String        walletSessionToken; // Session token for wallet operations

    private UserInfo      user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long          id;
        private String        userName;
        private String        fullName;
        private String        email;
        private String        phone;
        private String        image;
        private Gender        gender;
        private String        googleId;
        private Boolean       active;
        private Boolean       isEmployee;
        private Boolean       isDeleted;
        private Boolean       isWalletExist;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<String>  roles;
    }
}
