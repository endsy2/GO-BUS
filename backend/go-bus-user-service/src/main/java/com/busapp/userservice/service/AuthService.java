package com.busapp.userservice.service;

import com.busapp.userservice.dto.request.AuthRequest;
import com.busapp.userservice.dto.response.AuthResponse;
import com.busapp.userservice.dto.response.RefreshTokenResponse;
import com.busapp.userservice.dto.response.RegisterResponse;
import com.busapp.userservice.dto.request.UserRequest;

public interface AuthService {
    RegisterResponse register(UserRequest request);
    AuthResponse login(AuthRequest request);
    RefreshTokenResponse refresh(String refreshToken);
    void logout(String accessToken, String refreshToken);
}
