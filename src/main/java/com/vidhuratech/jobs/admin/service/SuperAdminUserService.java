package com.vidhuratech.jobs.admin.service;

import com.vidhuratech.jobs.admin.dto.CreateInternalUserRequest;
import com.vidhuratech.jobs.admin.dto.UpdateUserRequest;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuperAdminUserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public User create(CreateInternalUserRequest req) {
        if (repo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        user.setActive(true);

        return repo.save(user);
    }

    public Page<User> getAll(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (search != null && !search.isBlank()) {
            return repo.findByNameContainingIgnoreCase(search, pageable);
        }

        return repo.findAll(pageable);
    }

    public User update(Long id, UpdateUserRequest req) {
        User user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(req.getName());
        user.setPhone(req.getPhone());
        user.setRole(req.getRole());
        user.setActive(req.getActive());

        return repo.save(user);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public void resetPassword(Long id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(encoder.encode("Admin@123"));
        repo.save(user);
    }
}