package com.busapp.userservice.dto.mapper;

import com.busapp.userservice.dto.admin.PermissionRequest;
import com.busapp.userservice.dto.admin.PermissionResponse;
import com.busapp.userservice.model.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionResponse toResponse(Permission permission) {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setName(permission.getName());
        response.setDescription(permission.getDescription());
        return response;
    }

    public Permission toEntity(PermissionRequest request) {
        Permission permission = new Permission();
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        return permission;
    }
}
