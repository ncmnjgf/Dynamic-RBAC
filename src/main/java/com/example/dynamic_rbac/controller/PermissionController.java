package com.example.dynamic_rbac.controller;

import com.example.dynamic_rbac.dto.ApiResponse;
import com.example.dynamic_rbac.dto.PermissionRequest;
import com.example.dynamic_rbac.dto.PermissionResponse;
import com.example.dynamic_rbac.service.RbacService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final RbacService rbacService;

    @PostMapping
    @PreAuthorize("hasPermission(#request, 'PERMISSION_CREATE')")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(@Valid @RequestBody PermissionRequest request) {
        PermissionResponse response = rbacService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PermissionResponse>builder()
                        .success(true)
                        .message("Permission created successfully")
                        .data(response)
                        .build());
    }
}
