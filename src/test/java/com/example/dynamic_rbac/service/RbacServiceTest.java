package com.example.dynamic_rbac.service;

import com.example.dynamic_rbac.dto.PermissionRequest;
import com.example.dynamic_rbac.dto.PermissionResponse;
import com.example.dynamic_rbac.dto.RoleRequest;
import com.example.dynamic_rbac.dto.RoleResponse;
import com.example.dynamic_rbac.exception.BadRequestException;
import com.example.dynamic_rbac.exception.ResourceNotFoundException;
import com.example.dynamic_rbac.model.*;
import com.example.dynamic_rbac.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RbacServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private RbacService rbacService;

    @Test
    @DisplayName("Create role successfully")
    void testCreateRole_Success() {
        RoleRequest request = new RoleRequest("MANAGER");
        when(roleRepository.existsByName("MANAGER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(i -> {
            Role r = i.getArgument(0);
            r.setId(10L);
            return r;
        });

        RoleResponse response = rbacService.createRole(request);

        assertNotNull(response);
        assertEquals("MANAGER", response.getName());
        assertEquals(10L, response.getId());
    }

    @Test
    @DisplayName("Create role throws BadRequestException when duplicate")
    void testCreateRole_Duplicate() {
        RoleRequest request = new RoleRequest("ADMIN");
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> rbacService.createRole(request));
    }

    @Test
    @DisplayName("Create permission successfully")
    void testCreatePermission_Success() {
        PermissionRequest request = new PermissionRequest("REPORT_VIEW");
        when(permissionRepository.existsByName("REPORT_VIEW")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenAnswer(i -> {
            Permission p = i.getArgument(0);
            p.setId(20L);
            return p;
        });

        PermissionResponse response = rbacService.createPermission(request);

        assertNotNull(response);
        assertEquals("REPORT_VIEW", response.getName());
        assertEquals(20L, response.getId());
    }

    @Test
    @DisplayName("Create permission throws BadRequestException when duplicate")
    void testCreatePermission_Duplicate() {
        PermissionRequest request = new PermissionRequest("ROLE_CREATE");
        when(permissionRepository.existsByName("ROLE_CREATE")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> rbacService.createPermission(request));
    }

    @Test
    @DisplayName("Assign permission to role successfully")
    void testAssignPermissionToRole_Success() {
        Role role = Role.builder().id(1L).name("ADMIN").build();
        Permission permission = Permission.builder().id(2L).name("CUSTOM_PERM").build();

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(2L)).thenReturn(Optional.of(permission));
        when(rolePermissionRepository.existsByRoleAndPermission(role, permission)).thenReturn(false);

        RoleResponse response = rbacService.assignPermissionToRole(1L, 2L);

        assertNotNull(response);
        assertTrue(response.getPermissions().contains("CUSTOM_PERM"));
        verify(rolePermissionRepository, times(1)).save(any(RolePermission.class));
    }

    @Test
    @DisplayName("Assign permission to role throws ResourceNotFound when role not found")
    void testAssignPermissionToRole_RoleNotFound() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rbacService.assignPermissionToRole(99L, 1L));
    }

    @Test
    @DisplayName("Assign role to user successfully")
    void testAssignRoleToUser_Success() {
        User user = User.builder().id(1L).username("testuser").build();
        Role role = Role.builder().id(2L).name("USER").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));
        when(userRoleRepository.existsByUserAndRole(user, role)).thenReturn(false);

        assertDoesNotThrow(() -> rbacService.assignRoleToUser(1L, 2L));
        verify(userRoleRepository, times(1)).save(any(UserRole.class));
    }

    @Test
    @DisplayName("Assign role to user throws BadRequestException when already assigned")
    void testAssignRoleToUser_AlreadyAssigned() {
        User user = User.builder().id(1L).username("testuser").build();
        Role role = Role.builder().id(2L).name("USER").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));
        when(userRoleRepository.existsByUserAndRole(user, role)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> rbacService.assignRoleToUser(1L, 2L));
    }
}
