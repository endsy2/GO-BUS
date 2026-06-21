package com.busapp.userservice.controller;

import com.busapp.userservice.dto.response.ApiResponse;
import com.busapp.userservice.dto.admin.PermissionRequest;
import com.busapp.userservice.dto.admin.PermissionResponse;
import com.busapp.userservice.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin endpoints for permission management.
 */
@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Permissions retrieved successfully",
                permissionService.getAllPermissions()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Permission retrieved successfully",
                permissionService.getPermissionById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED.value(), "Permission created successfully",
                        permissionService.createPermission(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Permission updated successfully",
                permissionService.updatePermission(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Permission deleted successfully", null));
    }
}
