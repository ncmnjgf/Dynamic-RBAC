package com.example.dynamic_rbac.config;

import com.example.dynamic_rbac.model.*;
import com.example.dynamic_rbac.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized, skipping seeding.");
            return;
        }

        log.info("Initializing database with roles, permissions, and users...");

        // 1. Create Permissions
        Permission pRoleCreate = createPermissionIfNotExists("ROLE_CREATE");
        Permission pPermCreate = createPermissionIfNotExists("PERMISSION_CREATE");
        Permission pRolePermAssign = createPermissionIfNotExists("ROLE_PERMISSION_ASSIGN");
        Permission pUserRoleAssign = createPermissionIfNotExists("USER_ROLE_ASSIGN");
        Permission pSecureRead = createPermissionIfNotExists("SECURE_DATA_READ");

        // 2. Create Roles
        Role adminRole = createRoleIfNotExists("ADMIN");
        Role userRole = createRoleIfNotExists("USER");

        // 3. Assign Permissions to Roles
        // ADMIN permissions
        assignPermission(adminRole, pRoleCreate);
        assignPermission(adminRole, pPermCreate);
        assignPermission(adminRole, pRolePermAssign);
        assignPermission(adminRole, pUserRoleAssign);
        // ADMIN can also read secure data
        assignPermission(adminRole, pSecureRead);

        // USER permissions
        assignPermission(userRole, pSecureRead);

        // 4. Create Users (admin / admin123, user / user123)
        User adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .build();
        userRepository.save(adminUser);

        User regularUser = User.builder()
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .build();
        userRepository.save(regularUser);

        // 5. Assign Roles to Users
        assignRole(adminUser, adminRole);
        assignRole(regularUser, userRole);

        log.info("Seeding completed successfully: Created admin (role: ADMIN) and user (role: USER)");
    }

    private Permission createPermissionIfNotExists(String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(Permission.builder().name(name).build()));
    }

    private Role createRoleIfNotExists(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
    }

    private void assignPermission(Role role, Permission permission) {
        if (!rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
            rolePermissionRepository.save(RolePermission.builder().role(role).permission(permission).build());
        }
    }

    private void assignRole(User user, Role role) {
        if (!userRoleRepository.existsByUserAndRole(user, role)) {
            userRoleRepository.save(UserRole.builder().user(user).role(role).build());
        }
    }
}
