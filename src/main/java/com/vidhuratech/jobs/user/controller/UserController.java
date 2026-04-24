package com.vidhuratech.jobs.user.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.user.dto.CreateEmployeeDTO;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.repository.UserRepository;
import com.vidhuratech.jobs.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping
    public ApiResponse<?> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<User> users;

        Pageable pageable = PageRequest.of(page, size);

        if (role != null && !role.isBlank()) {
            users = userRepository.findByRole(UserRole.valueOf(role), pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            users = userRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        var content = users.getContent().stream()
                .map(u -> Map.of(
                        "id", u.getId(),
                        "name", u.getName(),
                        "email", u.getEmail(),
                        "role", u.getRole(),
                        "active", u.getActive()
                ))
                .toList();

        return ApiResponse.success(Map.of(
                "content", content,
                "totalPages", users.getTotalPages()
        ));
    }

    @GetMapping("/students/search")
    public ApiResponse<?> searchStudents(
            @RequestParam String keyword
    ) {
        var users = userRepository
                .findByRoleAndNameContainingIgnoreCase(
                        UserRole.STUDENT,
                        keyword
                );

        var data = users.stream()
                .map(u -> Map.of(
                        "id", u.getId(),
                        "name", u.getName(),
                        "email", u.getEmail()
                ))
                .toList();

        return ApiResponse.builder()
                .success(true)
                .data(data)
                .build();
    }

    @PostMapping("/employees")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<?> createEmployee(@RequestBody CreateEmployeeDTO dto) {
        return ApiResponse.success(
                userService.createEmployee(dto),
                "Employee created. Setup link sent."
        );
    }
}