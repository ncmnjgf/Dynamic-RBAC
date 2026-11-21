package com.example.dynamic_rbac.controller;

import com.example.dynamic_rbac.dto.PermissionRequest;
import com.example.dynamic_rbac.dto.RoleRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class DynamicRbacIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Admin can create a role")
    @WithMockUser(username = "admin")
    void testCreateRole_WithAdminUser() throws Exception {
        RoleRequest request = new RoleRequest("SUPERVISOR");

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("SUPERVISOR"));
    }

    @Test
    @DisplayName("Regular user cannot create a role (403 Forbidden)")
    @WithMockUser(username = "user")
    void testCreateRole_WithRegularUser_Forbidden() throws Exception {
        RoleRequest request = new RoleRequest("GUEST");

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Admin can create a permission")
    @WithMockUser(username = "admin")
    void testCreatePermission_WithAdminUser() throws Exception {
        PermissionRequest request = new PermissionRequest("AUDIT_LOG_EXPORT");

        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("AUDIT_LOG_EXPORT"));
    }

    @Test
    @DisplayName("Regular user cannot create a permission (403 Forbidden)")
    @WithMockUser(username = "user")
    void testCreatePermission_WithRegularUser_Forbidden() throws Exception {
        PermissionRequest request = new PermissionRequest("AUDIT_LOG_EXPORT_FAIL");

        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Admin can assign permission to role")
    @WithMockUser(username = "admin")
    void testAssignPermissionToRole_WithAdminUser() throws Exception {
        // ID 2 is USER role, ID 1 is ROLE_CREATE in DataInitializer
        mockMvc.perform(post("/roles/2/permissions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Admin can assign role to user")
    @WithMockUser(username = "admin")
    void testAssignRoleToUser_WithAdminUser() throws Exception {
        // Assign ADMIN role (ID 1) to regular user (ID 2)
        mockMvc.perform(post("/users/2/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("User with SECURE_DATA_READ permission can access /secure-data")
    @WithMockUser(username = "user")
    void testGetSecureData_WithAuthorizedUser() throws Exception {
        mockMvc.perform(get("/secure-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessedBy").value("user"));
    }

    @Test
    @DisplayName("Unauthenticated request to /secure-data is rejected (401 Unauthorized)")
    void testGetSecureData_Unauthenticated() throws Exception {
        mockMvc.perform(get("/secure-data"))
                .andExpect(status().isUnauthorized());
    }
}
