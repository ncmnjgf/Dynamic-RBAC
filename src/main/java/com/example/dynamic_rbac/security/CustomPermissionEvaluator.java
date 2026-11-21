package com.example.dynamic_rbac.security;

import com.example.dynamic_rbac.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Set;

/**
 * Custom PermissionEvaluator that dynamically verifies permissions against the database
 * at runtime. This avoids any hardcoded role checks (e.g. hasRole('ADMIN')).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final PermissionRepository permissionRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated() || permission == null) {
            return false;
        }

        String targetPermission = permission.toString().trim();
        String username = authentication.getName();

        // Dynamically fetch current permissions from database for real-time changes
        Set<String> userPermissions = permissionRepository.findPermissionNamesByUsername(username);

        boolean granted = userPermissions.contains(targetPermission);
        log.debug("Evaluating permission '{}' for user '{}' on target '{}': {}",
                targetPermission, username, targetDomainObject, granted);

        return granted;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated() || permission == null) {
            return false;
        }

        String targetPermission = permission.toString().trim();
        String username = authentication.getName();

        Set<String> userPermissions = permissionRepository.findPermissionNamesByUsername(username);

        boolean granted = userPermissions.contains(targetPermission);
        log.debug("Evaluating permission '{}' for user '{}' with targetId '{}' and targetType '{}': {}",
                targetPermission, username, targetId, targetType, granted);

        return granted;
    }
}
