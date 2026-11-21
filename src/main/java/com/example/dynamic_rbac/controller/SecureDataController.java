package com.example.dynamic_rbac.controller;

import com.example.dynamic_rbac.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/secure-data")
public class SecureDataController {

    @GetMapping
    @PreAuthorize("hasPermission(null, 'SECURE_DATA_READ')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSecureData(Authentication authentication) {
        Map<String, Object> data = Map.of(
                "title", "Confidential Classified Data",
                "accessedBy", authentication != null ? authentication.getName() : "anonymous",
                "message", "Dynamic RBAC authorization granted successfully!"
        );

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Secure data fetched successfully")
                .data(data)
                .build());
    }
}
