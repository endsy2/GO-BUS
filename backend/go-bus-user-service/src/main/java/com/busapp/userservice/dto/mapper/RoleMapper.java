package com.busapp.userservice.dto.mapper;

import com.busapp.userservice.dto.admin.RoleRequest;
import com.busapp.userservice.dto.admin.RoleResponse;
import com.busapp.userservice.model.Permission;
import com.busapp.userservice.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public RoleResponse toResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        response.setDescription(role.getDescription());
        response.setCreatedAt(role.getCreatedAt());
        response.setPermissions(role.getPermissions() == null ? List.of() :
                role.getPermissions().stream()
                        .map(permissionMapper::toResponse)
                        .collect(Collectors.toList()));
        return response;
    }

    public Role toEntity(RoleRequest request, Set<Permission> resolvedPermissions) {
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(resolvedPermissions);
        return role;
    }
}
