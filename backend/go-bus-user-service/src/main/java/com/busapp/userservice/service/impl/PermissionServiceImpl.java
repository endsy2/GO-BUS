package com.busapp.userservice.service.impl;

import com.busapp.userservice.dto.admin.PermissionRequest;
import com.busapp.userservice.dto.admin.PermissionResponse;
import com.busapp.userservice.exception.DuplicateResourceException;
import com.busapp.userservice.exception.ResourceNotFoundException;
import com.busapp.userservice.dto.mapper.PermissionMapper;
import com.busapp.userservice.model.Permission;
import com.busapp.userservice.repository.PermissionRepository;
import com.busapp.userservice.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper     permissionMapper;

    @Override
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionResponse getPermissionById(Long id) {
        return permissionMapper.toResponse(findPermission(id));
    }

    @Override
    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        log.debug("PERMISSION_CREATE", kv("name", request.getName()));
        if (permissionRepository.existsByName(request.getName())) {
            log.debug("PERMISSION_CREATE_REJECTED", kv("name", request.getName()), kv("reason", "NAME_EXISTS"));
            throw new DuplicateResourceException("Permission already exists: " + request.getName());
        }
        PermissionResponse response = permissionMapper.toResponse(permissionRepository.save(permissionMapper.toEntity(request)));
        log.debug("PERMISSION_CREATED", kv("permissionId", response.getId()), kv("name", response.getName()));
        return response;
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(Long id, PermissionRequest request) {
        log.debug("PERMISSION_UPDATE", kv("permissionId", id));
        Permission perm = findPermission(id);
        perm.setName(request.getName());
        perm.setDescription(request.getDescription());
        PermissionResponse response = permissionMapper.toResponse(permissionRepository.save(perm));
        log.debug("PERMISSION_UPDATED", kv("permissionId", id), kv("name", response.getName()));
        return response;
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        log.debug("PERMISSION_DELETE", kv("permissionId", id));
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permission not found: " + id);
        }
        permissionRepository.deleteById(id);
        log.debug("PERMISSION_DELETED", kv("permissionId", id));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Permission findPermission(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + id));
    }
}
