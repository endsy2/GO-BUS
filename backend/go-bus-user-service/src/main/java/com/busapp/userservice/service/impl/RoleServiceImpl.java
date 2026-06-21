package com.busapp.userservice.service.impl;

import com.busapp.userservice.dto.admin.PagedResponse;
import com.busapp.userservice.dto.admin.RoleRequest;
import com.busapp.userservice.dto.admin.RoleResponse;
import com.busapp.userservice.exception.DuplicateResourceException;
import com.busapp.userservice.exception.ResourceNotFoundException;
import com.busapp.userservice.dto.mapper.RoleMapper;
import com.busapp.userservice.model.Permission;
import com.busapp.userservice.model.Role;
import com.busapp.userservice.repository.PermissionRepository;
import com.busapp.userservice.repository.RoleRepository;
import com.busapp.userservice.service.RolePermissionCacheService;
import com.busapp.userservice.service.RolePermissionNameCacheService;
import com.busapp.userservice.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository       roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper           roleMapper;
    private final RolePermissionCacheService rolePermissionCacheService;
    private final RolePermissionNameCacheService rolePermissionNameCacheService;

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponse<RoleResponse> getAllRolesPaged(int page, int size) {
        Page<Role> rolePage = roleRepository.findAll(PageRequest.of(page-1, size));
        List<RoleResponse> content = rolePage.getContent().stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<RoleResponse>builder()
                .content(content)
                .page(rolePage.getNumber())
                .size(rolePage.getSize())
                .totalElements(rolePage.getTotalElements())
                .totalPages(rolePage.getTotalPages())
                .build();
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        return roleMapper.toResponse(findRole(id));
    }

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        log.debug("ROLE_CREATE", kv("name", request.getName()),
                kv("permissionCount", request.getPermissions() == null ? 0 : request.getPermissions().size()));
        if (roleRepository.existsByName(request.getName())) {
            log.debug("ROLE_CREATE_REJECTED", kv("name", request.getName()), kv("reason", "NAME_EXISTS"));
            throw new DuplicateResourceException("Role already exists: " + request.getName());
        }
        Role role = roleMapper.toEntity(request, resolvePermissions(request.getPermissions()));
        RoleResponse response = roleMapper.toResponse(roleRepository.save(role));
        clearAllRoleCaches();
        log.debug("ROLE_CREATED", kv("roleId", response.getId()), kv("name", response.getName()));
        return response;
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        log.debug("ROLE_UPDATE", kv("roleId", id));
        Role role = findRole(id);
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        if (request.getPermissions() != null) {
            role.setPermissions(resolvePermissions(request.getPermissions()));
        }
        RoleResponse response = roleMapper.toResponse(roleRepository.save(role));
        clearAllRoleCaches();
        log.debug("ROLE_UPDATED", kv("roleId", id), kv("name", response.getName()));
        return response;
    }

    @Override
    @Transactional
    public RoleResponse assignPermissions(Long roleId, List<String> permissionNames) {
        log.debug("ROLE_ASSIGN_PERMISSIONS", kv("roleId", roleId),
                kv("permissionCount", permissionNames == null ? 0 : permissionNames.size()));
        Role role = findRole(roleId);
        role.setPermissions(resolvePermissions(permissionNames));
        RoleResponse response = roleMapper.toResponse(roleRepository.save(role));
        clearAllRoleCaches();
        log.debug("ROLE_PERMISSIONS_ASSIGNED", kv("roleId", roleId));
        return response;
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        log.debug("ROLE_DELETE", kv("roleId", id));
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role not found: " + id);
        }
        roleRepository.deleteById(id);
        clearAllRoleCaches();
        log.debug("ROLE_DELETED", kv("roleId", id));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void clearAllRoleCaches() {
        rolePermissionCacheService.clearRoleCache();
        rolePermissionNameCacheService.clearCache();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Role findRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
    }

    private Set<Permission> resolvePermissions(List<String> names) {
        if (names == null || names.isEmpty()) return new HashSet<>();
        return new HashSet<>(permissionRepository.findByNameIn(new HashSet<>(names)));
    }
}
