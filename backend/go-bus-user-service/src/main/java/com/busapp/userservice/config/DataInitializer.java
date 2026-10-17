package com.busapp.userservice.config;

import com.busapp.userservice.model.Permission;
import com.busapp.userservice.model.Role;
import com.busapp.userservice.model.User;
import com.busapp.userservice.repository.PermissionRepository;
import com.busapp.userservice.repository.RoleRepository;
import com.busapp.userservice.repository.UserRepository;
import com.busapp.userservice.service.RolePermissionCacheService;
import com.busapp.userservice.service.RolePermissionNameCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Initializes default data when the application starts.
 * Creates the default permissions, the admin role (granted every default permission)
 * and the admin user if they don't exist. Safe to run on every startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    /**
     * Permission catalogue enforced by the gateway's RouteAuthorizationFilter.
     * Keep in sync with that filter and with seed_roles_permissions.sql.
     */
    private static final Map<String, String> DEFAULT_PERMISSIONS = new LinkedHashMap<>();

    static {
        DEFAULT_PERMISSIONS.put("USER_READ",          "Read any user profile (admin)");
        DEFAULT_PERMISSIONS.put("USER_WRITE",         "Update any user (admin)");
        DEFAULT_PERMISSIONS.put("USER_DELETE",        "Delete any user (admin)");
        DEFAULT_PERMISSIONS.put("WALLET_READ",        "Read own wallet & transactions");
        DEFAULT_PERMISSIONS.put("WALLET_WRITE",       "Top-up / modify wallet");
        DEFAULT_PERMISSIONS.put("NOTIFICATION_READ",  "Read own notifications");
        DEFAULT_PERMISSIONS.put("NOTIFICATION_WRITE", "Mark notifications as read");
        DEFAULT_PERMISSIONS.put("BUS_READ",           "Read buses, routes, schedules, seats, layouts");
        DEFAULT_PERMISSIONS.put("BUS_WRITE",          "Create / update buses, routes, schedules, seats, layouts");
        DEFAULT_PERMISSIONS.put("BUS_DELETE",         "Delete buses, routes, schedules, seats, layouts");
        DEFAULT_PERMISSIONS.put("BOOKING_READ",       "Read own bookings");
        DEFAULT_PERMISSIONS.put("BOOKING_WRITE",      "Create / update bookings");
        DEFAULT_PERMISSIONS.put("BOOKING_DELETE",     "Cancel a booking");
        DEFAULT_PERMISSIONS.put("PAYMENT_READ",       "Read payments");
        DEFAULT_PERMISSIONS.put("PAYMENT_WRITE",      "Initiate a payment");
        DEFAULT_PERMISSIONS.put("TICKET_READ",        "View tickets");
        DEFAULT_PERMISSIONS.put("PROMO_READ",         "Read promo codes");
        DEFAULT_PERMISSIONS.put("PROMO_MANAGE",       "Create / update / delete promo codes");
        DEFAULT_PERMISSIONS.put("ADMIN_ACCESS",       "Unrestricted admin access");
    }

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolePermissionCacheService rolePermissionCacheService;
    private final RolePermissionNameCacheService rolePermissionNameCacheService;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void run(String... args) {
        // One transaction so Role.permissions (LAZY) can be read and updated.
        transactionTemplate.executeWithoutResult(status -> {
            Set<Permission> defaultPermissions = initializeDefaultPermissions();
            Role adminRole = initializeDefaultRoles();
            grantPermissions(adminRole, defaultPermissions);
            initializeDefaultAdminUser(adminRole);
        });

        // Cached role -> permission names would otherwise keep serving the old
        // (possibly empty) list in the JWT for up to an hour. Cleared after commit
        // so a concurrent login can't re-cache the pre-commit state.
        clearRolePermissionCaches();
    }

    private Set<Permission> initializeDefaultPermissions() {
        Set<Permission> permissions = new HashSet<>(permissionRepository.findByNameIn(DEFAULT_PERMISSIONS.keySet()));
        Set<String> existingNames = permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        List<Permission> missing = DEFAULT_PERMISSIONS.entrySet().stream()
                .filter(entry -> !existingNames.contains(entry.getKey()))
                .map(entry -> Permission.builder()
                        .name(entry.getKey())
                        .description(entry.getValue())
                        .build())
                .toList();

        if (missing.isEmpty()) {
            log.info("Default permissions already exist ({})", permissions.size());
        } else {
            permissions.addAll(permissionRepository.saveAll(missing));
            log.info("Default permissions created: {}", missing.stream().map(Permission::getName).toList());
        }
        return permissions;
    }

    private Role initializeDefaultRoles() {
        String adminRoleName = "ROLE_ADMIN";

        if (!roleRepository.existsByName(adminRoleName)) {
            Role adminRole = Role.builder()
                    .name(adminRoleName)
                    .description("Administrator role with full system access")
                    .build();

            adminRole = roleRepository.save(adminRole);
            log.info("Default admin role created: {}", adminRoleName);
            return adminRole;
        } else {
            log.info("Admin role already exists: {}", adminRoleName);
            return roleRepository.findByName(adminRoleName).orElseThrow();
        }
    }

    /**
     * Adds any of the given permissions the role is missing. Never removes
     * permissions, so extra ones assigned through the admin API are kept.
     */
    private void grantPermissions(Role role, Set<Permission> permissions) {
        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }
        Set<String> grantedNames = role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        List<Permission> missing = permissions.stream()
                .filter(permission -> !grantedNames.contains(permission.getName()))
                .toList();

        if (missing.isEmpty()) {
            log.info("Role {} already has all default permissions", role.getName());
            return;
        }
        role.getPermissions().addAll(missing);
        roleRepository.save(role);
        log.info("Granted {} permission(s) to {}: {}", missing.size(), role.getName(),
                missing.stream().map(Permission::getName).sorted().toList());
    }

    private void initializeDefaultAdminUser(Role adminRole) {
        String adminEmail = "admin@busapp.com";
        String adminUsername = "admin";

        if (!userRepository.existsByEmail(adminEmail)) {
            User adminUser = User.builder()
                    .userName(adminUsername)
                    .fullName("System Administrator")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .active(true)
                    .isEmployee(true)
                    .isDeleted(false)
                    .isWalletExist(false)
                    .roles(Set.of(adminRole))
                    .build();

            userRepository.save(adminUser);
            log.info("Default admin user created - Email: {}, Username: {}", adminEmail, adminUsername);
            log.warn("IMPORTANT: Change the default admin password immediately!");
        } else {
            log.info("Admin user already exists: {}", adminEmail);
        }
    }

    private void clearRolePermissionCaches() {
        try {
            rolePermissionCacheService.clearRoleCache();
            rolePermissionNameCacheService.clearCache();
        } catch (RuntimeException e) {
            // Don't block startup if Redis is unreachable — entries expire within 1 hour.
            log.warn("Could not clear role/permission caches on startup: {}", e.getMessage());
        }
    }
}
