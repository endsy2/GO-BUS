package com.busapp.userservice.service;

import com.busapp.userservice.dto.admin.PermissionResponse;
import com.busapp.userservice.dto.admin.RoleResponse;
import com.busapp.userservice.model.Permission;
import com.busapp.userservice.model.Role;
import com.busapp.userservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for caching role and permission mappings to avoid repeated database queries.
 * Roles and permissions change infrequently, making them ideal for caching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionCacheService {

    private final RoleRepository roleRepository;

    /**
     * Get role with permissions by role ID (cached for 1 hour)
     */
    @Cacheable(value = "roleWithPermissions", key = "#roleId")
    public RoleResponse getRoleWithPermissions(Long roleId) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            return null;
        }
        return mapToRoleResponse(role);
    }

    /**
     * Get multiple roles with permissions by role IDs (cached)
     */
    @Cacheable(value = "rolesWithPermissions", key = "#roleIds.toString()")
    public Set<RoleResponse> getRolesWithPermissions(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Set.of();
        }
        
        return roleRepository.findAllById(roleIds).stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toSet());
    }

    /**
     * Clear role cache when roles are updated
     */
    @CacheEvict(value = {"roleWithPermissions", "rolesWithPermissions"}, allEntries = true)
    public void clearRoleCache() {
        log.info("Role and permission cache cleared");
    }

    /**
     * Map Role entity to RoleResponse DTO
     */
    private RoleResponse mapToRoleResponse(Role role) {
        List<PermissionResponse> permissions = role.getPermissions() == null ? List.of() :
                role.getPermissions().stream()
                        .map(this::mapToPermissionResponse)
                        .collect(Collectors.toList());

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .permissions(permissions)
                .build();
    }

    /**
     * Map Permission entity to PermissionResponse DTO
     */
    private PermissionResponse mapToPermissionResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }
}
