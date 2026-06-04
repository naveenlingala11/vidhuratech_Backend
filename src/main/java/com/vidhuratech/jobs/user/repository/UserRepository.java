package com.vidhuratech.jobs.user.repository;

import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByDeletedFalse(Pageable pageable);

    Page<User> findByDeletedFalseAndRole(UserRole role, Pageable pageable);

    Page<User> findByDeletedFalseAndNameContainingIgnoreCase(String name, Pageable pageable);

    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<User> findByRole(UserRole role, Pageable pageable);

    List<User> findByRoleAndNameContainingIgnoreCase(UserRole role, String name);

    long countByActiveTrue();

    long countByActiveFalse();

    long countByRole(UserRole role);

    long countByDeletedFalse();

    long countByDeletedTrue();

    long countByDeletedFalseAndActiveTrue();

    long countByDeletedFalseAndActiveFalse();

    List<User> findByRoleAndDeletedFalseAndActiveTrue(UserRole role);

    Optional<User> findByIdAndRoleAndDeletedFalseAndActiveTrue(Long id, UserRole role);

    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

}