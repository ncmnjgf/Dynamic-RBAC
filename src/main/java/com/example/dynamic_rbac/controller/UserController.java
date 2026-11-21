package com.example.dynamic_rbac.controller;

import com.example.dynamic_rbac.dto.ApiResponse;
import com.example.dynamic_rbac.service.RbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final RbacService rbacService;

    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasPermission(null, 'USER_ROLE_ASSIGN')")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        rbacService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Role assigned to user successfully")
                .build());
    }
}
