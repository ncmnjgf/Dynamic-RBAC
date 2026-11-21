package com.example.dynamic_rbac.service;

import com.example.dynamic_rbac.dto.*;
import com.example.dynamic_rbac.exception.BadRequestException;
import com.example.dynamic_rbac.exception.ResourceNotFoundException;
import com.example.dynamic_rbac.model.*;
import com.example.dynamic_rbac.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RbacService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        String roleName = request.getName().trim().toUpperCase();
        if (roleRepository.existsByName(roleName)) {
            throw new BadRequestException("Role already exists with name: " + roleName);
        }

        Role role = Role.builder()
                .name(roleName)
                .build();
        Role saved = roleRepository.save(role);
        log.info("Created new role: {} (ID: {})", saved.getName(), saved.getId());

        return RoleResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .permissions(Set.of())
                .build();
    }

    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        String permName = request.getName().trim().toUpperCase();
        if (permissionRepository.existsByName(permName)) {
            throw new BadRequestException("Permission already exists with name: " + permName);
        }

        Permission permission = Permission.builder()
                .name(permName)
                .build();
        Permission saved = permissionRepository.save(permission);
        log.info("Created new permission: {} (ID: {})", saved.getName(), saved.getId());

        return PermissionResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .build();
    }

    @Transactional
    public RoleResponse assignPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId));

        if (rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
            throw new BadRequestException("Permission '" + permission.getName() + "' is already assigned to role '" + role.getName() + "'");
        }

        RolePermission rolePermission = RolePermission.builder()
                .role(role)
                .permission(permission)
                .build();
        rolePermissionRepository.save(rolePermission);
        log.info("Assigned permission '{}' to role '{}'", permission.getName(), role.getName());

        Set<String> permissions = role.getRolePermissions().stream()
                .map(rp -> rp.getPermission().getName())
                .collect(Collectors.toSet());
        permissions.add(permission.getName());

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(permissions)
                .build();
    }

    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        if (userRoleRepository.existsByUserAndRole(user, role)) {
            throw new BadRequestException("Role '" + role.getName() + "' is already assigned to user '" + user.getUsername() + "'");
        }

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .build();
        userRoleRepository.save(userRole);
        log.info("Assigned role '{}' to user '{}'", role.getName(), user.getUsername());
    }
}
