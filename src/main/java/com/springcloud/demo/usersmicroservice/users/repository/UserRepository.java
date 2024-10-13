package com.springcloud.demo.usersmicroservice.users.repository;

import com.springcloud.demo.usersmicroservice.users.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u " +
            "WHERE u.name ILIKE %?1% " +
            "OR u.email ILIKE %?1"
    )
    Page<User> findBySearchTerm(String searchTerm, Pageable pageable);

    Optional<User> findByEmail(String email);
}
