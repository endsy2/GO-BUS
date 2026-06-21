package com.busapp.userservice.service;

import com.busapp.userservice.model.Permission;
import com.busapp.userservice.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for caching role and permission names for JWT token generation.
 * Avoids expensive stream operations on every login.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionNameCacheService {

    /**
     * Get role names and permission names for a set of roles (cached).
     * Returns a map with "roleNames" and "permissionNames" keys.
     */
    @Cacheable(value = "rolePermissionNames", key = "#roleIds.toString()")
    public Map<String, List<String>> getRoleAndPermissionNames(Set<Long> roleIds, Set<Role> roles) {
        List<String> roleNames = new ArrayList<>();
        Set<String> permissionSet = new HashSet<>();

        for (Role role : roles) {
            roleNames.add(role.getName());
            if (role.getPermissions() != null) {
                for (Permission permission : role.getPermissions()) {
                    permissionSet.add(permission.getName());
                }
            }
        }

        Collections.sort(roleNames);
        List<String> permissionNames = new ArrayList<>(permissionSet);
        Collections.sort(permissionNames);

        Map<String, List<String>> result = new HashMap<>();
        result.put("roleNames", roleNames);
        result.put("permissionNames", permissionNames);
        
        return result;
    }

    /**
     * Clear cache when roles or permissions are updated
     */
    @CacheEvict(value = "rolePermissionNames", allEntries = true)
    public void clearCache() {
        log.info("Role and permission names cache cleared");
    }
}
