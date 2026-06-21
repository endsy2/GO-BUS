package com.busapp.userservice.controller;

import com.busapp.userservice.dto.admin.AssignPermissionsRequest;
import com.busapp.userservice.dto.admin.PagedResponse;
import com.busapp.userservice.dto.response.ApiResponse;
import com.busapp.userservice.dto.admin.RoleRequest;
import com.busapp.userservice.dto.admin.RoleResponse;
import com.busapp.userservice.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin endpoints for role management.
 */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Roles retrieved successfully",
                roleService.getAllRoles()));
    }

    @GetMapping("/paged")
    public ResponseEntity<PagedResponse<RoleResponse>> getAllRolesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(roleService.getAllRolesPaged(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Role retrieved successfully",
                roleService.getRoleById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED.value(), "Role created successfully", roleService.createRole(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Role updated successfully",
                roleService.updateRole(id, request)));
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermissions(
            @PathVariable Long id,
            @Valid @RequestBody AssignPermissionsRequest request) {
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(),
                "Permissions assigned successfully",
                roleService.assignPermissions(id, request.getPermissions())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Role deleted successfully", null));
    }
}
