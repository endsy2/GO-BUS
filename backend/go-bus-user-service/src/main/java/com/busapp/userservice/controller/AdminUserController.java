package com.busapp.userservice.controller;

import com.busapp.userservice.dto.response.ApiResponse;
import com.busapp.userservice.dto.admin.*;
import com.busapp.userservice.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin endpoints for user management.
 * All routes require ADMIN_ACCESS permission (enforced by RouteAuthorizationFilter).
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    // ── List / Filter ─────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AdminUserResponse>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String walletStatus,
            @RequestParam(required = false) Boolean isEmployee,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        AdminUserFilterRequest filter = AdminUserFilterRequest.builder()
                .search(search)
                .walletStatus(walletStatus)
                .active(active)
                .isEmployee(isEmployee)
                .fromDate(fromDate)
                .toDate(toDate)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Users retrieved successfully",
                adminUserService.getUsers(filter)));
    }

    // ── Get by ID ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "User retrieved successfully",
                adminUserService.getUserById(id)));
    }

    // ── Update User Info ──────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "User updated successfully",
                adminUserService.updateUser(id, request)));
    }

    // ── Set Active / Deactivate ───────────────────────────────────────────────

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AdminUserResponse>> setActive(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "User status updated",
                adminUserService.setActive(id, request.getActive())));
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<AdminUserResponse>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Password reset successfully",
                adminUserService.resetPassword(id, request.getNewPassword())));
    }

    // ── Unlink Google ─────────────────────────────────────────────────────────

    @PatchMapping("/{id}/unlink-google")
    public ResponseEntity<ApiResponse<AdminUserResponse>> unlinkGoogle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Google account unlinked",
                adminUserService.unlinkGoogle(id)));
    }

    // ── Assign Roles ──────────────────────────────────────────────────────────

    @PutMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<AdminUserResponse>> assignRoles(
            @PathVariable Long id,
            @Valid @RequestBody AssignRolesRequest request) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Roles assigned successfully",
                adminUserService.assignRoles(id, request.getRoles())));
    }

    // ── Wallet ────────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/wallet/adjust")
    public ResponseEntity<ApiResponse<AdminUserResponse>> adjustWallet(
            @PathVariable Long id,
            @Valid @RequestBody WalletAdjustRequest request) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Wallet adjusted successfully",
                adminUserService.adjustWallet(id, request)));
    }

    @PatchMapping("/{id}/wallet/status")
    public ResponseEntity<ApiResponse<AdminUserResponse>> setWalletStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Wallet status updated",
                adminUserService.setWalletStatus(id, status)));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "User deleted successfully", null));
    }
}
