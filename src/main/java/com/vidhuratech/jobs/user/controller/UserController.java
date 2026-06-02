package com.vidhuratech.jobs.user.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.user.dto.CreateEmployeeDTO;
import com.vidhuratech.jobs.user.dto.UpdateUserDTO;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.repository.UserRepository;
import com.vidhuratech.jobs.user.service.AdminPeopleActivityService;
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
    private final AdminPeopleActivityService adminPeopleActivityService;

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

    // NEW CODE
    @GetMapping("/advanced")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<?> getUsersAdvanced(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String active,
            @RequestParam(required = false) String deleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ApiResponse.success(
                userService.getUsers(role, keyword, active, deleted, page, size, sortBy, sortDir)
        );
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<?> getUserStats() {
        return ApiResponse.success(userService.getUserStats());
    }

    @GetMapping("/students/search")
    public ApiResponse<?> searchStudents(@RequestParam String keyword) {
        var users = userRepository.findByRoleAndNameContainingIgnoreCase(UserRole.STUDENT, keyword);

        var data = users.stream()
                .map(u -> Map.of(
                        "id", u.getId(),
                        "name", u.getName(),
                        "email", u.getEmail()
                ))
                .toList();

        return ApiResponse.builder().success(true).data(data).build();
    }

    @PostMapping("/employees")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<?> createEmployee(@RequestBody CreateEmployeeDTO dto) {
        return ApiResponse.success(userService.createEmployee(dto), "Employee created. Setup link sent.");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserDTO dto) {
        return ApiResponse.success(userService.updateUser(id, dto), "User updated successfully");
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return ApiResponse.success(userService.updateStatus(id, body.get("active")), "Status updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success(null, "User deleted successfully");
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<?> restoreUser(@PathVariable Long id) {
        return ApiResponse.success(userService.restoreUser(id), "User restored successfully");
    }

    @GetMapping("/people-360")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<?> people360(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminPeopleActivityService.people360(keyword));
    }

    @GetMapping("/people-360/{key}/history")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<?> peopleHistory(@PathVariable String key) {
        return ApiResponse.success(adminPeopleActivityService.history(key));
    }
}