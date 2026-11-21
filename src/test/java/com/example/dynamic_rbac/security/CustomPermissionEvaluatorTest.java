package com.example.dynamic_rbac.security;

import com.example.dynamic_rbac.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomPermissionEvaluatorTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomPermissionEvaluator permissionEvaluator;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Should return true when user has the requested permission")
    void testHasPermission_Granted() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(permissionRepository.findPermissionNamesByUsername("testuser"))
                .thenReturn(Set.of("ROLE_CREATE", "SECURE_DATA_READ"));

        boolean result = permissionEvaluator.hasPermission(authentication, null, "ROLE_CREATE");

        assertTrue(result);
        verify(permissionRepository, times(1)).findPermissionNamesByUsername("testuser");
    }

    @Test
    @DisplayName("Should return false when user does not have the requested permission")
    void testHasPermission_Denied() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(permissionRepository.findPermissionNamesByUsername("testuser"))
                .thenReturn(Set.of("SECURE_DATA_READ"));

        boolean result = permissionEvaluator.hasPermission(authentication, null, "ROLE_CREATE");

        assertFalse(result);
        verify(permissionRepository, times(1)).findPermissionNamesByUsername("testuser");
    }

    @Test
    @DisplayName("Should return false when authentication is null or unauthenticated")
    void testHasPermission_Unauthenticated() {
        assertFalse(permissionEvaluator.hasPermission(null, null, "ROLE_CREATE"));

        when(authentication.isAuthenticated()).thenReturn(false);
        assertFalse(permissionEvaluator.hasPermission(authentication, null, "ROLE_CREATE"));
    }

    @Test
    @DisplayName("Should return false when permission is null")
    void testHasPermission_NullPermission() {
        when(authentication.isAuthenticated()).thenReturn(true);
        assertFalse(permissionEvaluator.hasPermission(authentication, null, null));
    }

    @Test
    @DisplayName("Should evaluate permission with targetId and targetType overload")
    void testHasPermission_WithTargetIdAndType() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin");
        when(permissionRepository.findPermissionNamesByUsername("admin"))
                .thenReturn(Set.of("USER_ROLE_ASSIGN"));

        boolean granted = permissionEvaluator.hasPermission(authentication, 1L, "User", "USER_ROLE_ASSIGN");
        boolean notGranted = permissionEvaluator.hasPermission(authentication, 1L, "User", "UNKNOWN_PERM");

        assertTrue(granted);
        assertFalse(notGranted);
    }
}
