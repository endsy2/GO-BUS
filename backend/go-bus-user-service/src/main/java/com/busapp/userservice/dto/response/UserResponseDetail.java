package com.busapp.userservice.dto.response;

import com.busapp.userservice.dto.admin.RoleResponse;
import com.busapp.userservice.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDetail {
    private Long          id;
    private String        userName;
    private String        fullName;
    private String        email;
    private String        phone;
    private String        googleId;
    private String        image;
    private Gender        gender;
    private LocalDateTime createdAt;
    private Boolean      isEmployee;
    private Boolean      isDeleted;
    private Boolean      isActive;
    private String        profilePicture;
    private Boolean       isWalletExist;
    private Set<RoleResponse> roles;
}
