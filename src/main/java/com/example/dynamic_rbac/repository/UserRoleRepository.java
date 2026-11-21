package com.example.dynamic_rbac.repository;

import com.example.dynamic_rbac.model.Role;
import com.example.dynamic_rbac.model.User;
import com.example.dynamic_rbac.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    boolean existsByUserAndRole(User user, Role role);
    Optional<UserRole> findByUserAndRole(User user, Role role);
}
