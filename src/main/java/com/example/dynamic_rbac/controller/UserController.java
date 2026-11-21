package com.example.dynamic_rbac.controller;

import com.example.dynamic_rbac.dto.ApiResponse;
import com.example.dynamic_rbac.service.RbacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User role assignment endpoints")
public class UserController {

    private final RbacService rbacService;

    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasPermission(null, 'USER_ROLE_ASSIGN')")
    @Operation(summary = "Assign a role to a user", description = "Requires dynamic permission: USER_ROLE_ASSIGN")
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
