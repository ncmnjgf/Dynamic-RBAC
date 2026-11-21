package com.example.dynamic_rbac.controller;

import com.example.dynamic_rbac.dto.ApiResponse;
import com.example.dynamic_rbac.dto.RoleRequest;
import com.example.dynamic_rbac.dto.RoleResponse;
import com.example.dynamic_rbac.service.RbacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role management endpoints protected dynamically by dynamic permissions")
public class RoleController {

    private final RbacService rbacService;

    @PostMapping
    @PreAuthorize("hasPermission(#request, 'ROLE_CREATE')")
    @Operation(summary = "Create a new role", description = "Requires dynamic permission: ROLE_CREATE")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse response = rbacService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<RoleResponse>builder()
                        .success(true)
                        .message("Role created successfully")
                        .data(response)
                        .build());
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasPermission(null, 'ROLE_PERMISSION_ASSIGN')")
    @Operation(summary = "Assign permission to a role", description = "Requires dynamic permission: ROLE_PERMISSION_ASSIGN")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermissionToRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        RoleResponse response = rbacService.assignPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Permission assigned to role successfully")
                .data(response)
                .build());
    }
}
