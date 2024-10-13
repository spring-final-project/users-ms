package com.springcloud.demo.usersmicroservice.userroles.repository;

import com.springcloud.demo.usersmicroservice.userroles.model.UserRole;
import com.springcloud.demo.usersmicroservice.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    Optional<UserRole> findByUserAndRole(User user, String role);
}
