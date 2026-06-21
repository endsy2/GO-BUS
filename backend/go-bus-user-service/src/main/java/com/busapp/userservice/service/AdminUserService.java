package com.busapp.userservice.service;

import com.busapp.userservice.dto.admin.*;

import java.util.List;

public interface AdminUserService {

    PagedResponse<AdminUserResponse> getUsers(AdminUserFilterRequest filter);

    AdminUserResponse getUserById(Long userId);

    AdminUserResponse updateUser(Long userId, UpdateUserRequest request);

    AdminUserResponse setActive(Long userId, boolean active);

    AdminUserResponse resetPassword(Long userId, String newPassword);

    AdminUserResponse unlinkGoogle(Long userId);

    AdminUserResponse assignRoles(Long userId, List<String> roleNames);

    AdminUserResponse adjustWallet(Long userId, WalletAdjustRequest request);

    AdminUserResponse setWalletStatus(Long userId, String status);

    void deleteUser(Long userId);
}
