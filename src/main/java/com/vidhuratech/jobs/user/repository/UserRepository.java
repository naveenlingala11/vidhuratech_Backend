package com.vidhuratech.jobs.user.repository;

import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByDeletedFalse(Pageable pageable);

    Page<User> findByDeletedFalseAndRole(
            UserRole role,
            Pageable pageable
    );

    Page<User> findByDeletedFalseAndNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    long countByActiveTrue();

    long countByRole(UserRole role);

    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);

    long countByActiveFalse();

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findAll(Pageable pageable);

    List<User> findByRoleAndNameContainingIgnoreCase(UserRole role, String name);

}
