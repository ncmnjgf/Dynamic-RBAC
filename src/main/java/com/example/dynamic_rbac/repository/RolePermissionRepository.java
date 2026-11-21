package com.example.dynamic_rbac.repository;

import com.example.dynamic_rbac.model.Permission;
import com.example.dynamic_rbac.model.Role;
import com.example.dynamic_rbac.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    boolean existsByRoleAndPermission(Role role, Permission permission);
    Optional<RolePermission> findByRoleAndPermission(Role role, Permission permission);
}
