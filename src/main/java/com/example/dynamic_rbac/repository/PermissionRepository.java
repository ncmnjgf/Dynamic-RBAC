package com.example.dynamic_rbac.repository;

import com.example.dynamic_rbac.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    boolean existsByName(String name);

    @Query("""
        SELECT DISTINCT p.name
        FROM Permission p
        JOIN RolePermission rp ON rp.permission.id = p.id
        JOIN UserRole ur ON ur.role.id = rp.role.id
        WHERE ur.user.username = :username
    """)
    Set<String> findPermissionNamesByUsername(@Param("username") String username);
}
