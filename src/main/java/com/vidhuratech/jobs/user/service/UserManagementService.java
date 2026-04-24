package com.vidhuratech.jobs.user.service;

import com.vidhuratech.jobs.user.dto.CreateUserRequest;
import com.vidhuratech.jobs.user.dto.UserResponse;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.helper.RoleHierarchyUtil;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public void createUser(CreateUserRequest req, Long creatorId) {

        if (userRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        user.setCreatedBy(creatorId);

        userRepo.save(user);
    }

    public Page<UserResponse> getUsers(
            int page,
            int size,
            String search,
            UserRole role
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<User> users;

        if (search != null && !search.isBlank()) {
            users = userRepo.findByDeletedFalseAndNameContainingIgnoreCase(search, pageable);

        } else if (role != null) {
            users = userRepo.findByDeletedFalseAndRole(role, pageable);

        } else {
            users = userRepo.findByDeletedFalse(pageable);
        }

        return users.map(user ->
                new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole(),
                        user.getActive()
                )
        );
    }

    public void toggleStatus(Long id) {
        User user = userRepo.findById(id).orElseThrow();

        user.setActive(!user.getActive());

        userRepo.save(user);
    }

    public void softDelete(Long id) {
        User user = userRepo.findById(id).orElseThrow();

        user.setDeleted(true);

        userRepo.save(user);
    }
}