package com.busapp.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String  accessToken;
    private String  refreshToken;
    private String  tokenType;
    private long    accessTokenExpiresIn;   // milliseconds
    private long    refreshTokenExpiresIn;  // milliseconds
    private Long    userId;
    private String  email;
    private String  userName;
}
