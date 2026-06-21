package com.busapp.userservice.config;

import com.busapp.userservice.model.Role;
import com.busapp.userservice.model.User;
import com.busapp.userservice.repository.RoleRepository;
import com.busapp.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Initializes default data when the application starts.
 * Creates default admin role and admin user if they don't exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role adminRole = initializeDefaultRoles();
        initializeDefaultAdminUser(adminRole);
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
}
