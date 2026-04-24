package com.vidhuratech.jobs.user.controller;

import com.vidhuratech.jobs.user.dto.CreateUserRequest;
import com.vidhuratech.jobs.user.dto.UserResponse;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.repository.UserRepository;
import com.vidhuratech.jobs.user.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/super-admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class UserManagementController {

    private final UserManagementService service;
    private final UserRepository userRepo;

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody CreateUserRequest req,
            Authentication auth
    ) {
        User creator = userRepo.findByEmail(auth.getName()).orElseThrow();

        service.createUser(req, creator.getId());

        return ResponseEntity.ok("User Created Successfully");
    }

    @GetMapping
    public Page<UserResponse> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role
    ) {
        return service.getUsers(page, size, search, role);
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        service.toggleStatus(id);
        return ResponseEntity.ok("Status Updated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok("Deleted Successfully");
    }
}