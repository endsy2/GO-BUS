package com.busapp.userservice.service;

import com.busapp.userservice.dto.admin.PermissionRequest;
import com.busapp.userservice.dto.admin.PermissionResponse;

import java.util.List;

public interface PermissionService {

    List<PermissionResponse> getAllPermissions();

    PermissionResponse getPermissionById(Long id);

    PermissionResponse createPermission(PermissionRequest request);

    PermissionResponse updatePermission(Long id, PermissionRequest request);

    void deletePermission(Long id);
}
