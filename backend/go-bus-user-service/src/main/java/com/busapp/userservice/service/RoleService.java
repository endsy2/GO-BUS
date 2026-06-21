package com.busapp.userservice.service;

import com.busapp.userservice.dto.admin.PagedResponse;
import com.busapp.userservice.dto.admin.RoleRequest;
import com.busapp.userservice.dto.admin.RoleResponse;

import java.util.List;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    PagedResponse<RoleResponse> getAllRolesPaged(int page, int size);

    RoleResponse getRoleById(Long id);

    RoleResponse createRole(RoleRequest request);

    RoleResponse updateRole(Long id, RoleRequest request);

    RoleResponse assignPermissions(Long roleId, List<String> permissionNames);

    void deleteRole(Long id);
}
