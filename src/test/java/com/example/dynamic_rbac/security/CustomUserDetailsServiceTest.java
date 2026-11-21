package com.example.dynamic_rbac.security;

import com.example.dynamic_rbac.model.User;
import com.example.dynamic_rbac.repository.PermissionRepository;
import com.example.dynamic_rbac.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("Load user by username successfully")
    void testLoadUserByUsername_Success() {
        User user = User.builder()
                .username("john")
                .password("encoded_pass")
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(permissionRepository.findPermissionNamesByUsername("john")).thenReturn(Set.of("ROLE_CREATE"));

        UserDetails userDetails = userDetailsService.loadUserByUsername("john");

        assertNotNull(userDetails);
        assertEquals("john", userDetails.getUsername());
        assertEquals("encoded_pass", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CREATE")));
    }

    @Test
    @DisplayName("Load user by username throws UsernameNotFoundException when not found")
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("unknown"));
    }
}
