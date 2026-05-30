package com.vidhuratech.jobs.common.notification.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class ActivityNotificationController {

    private final ActivityNotificationService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> myNotifications() {
        return ApiResponse.builder()
                .success(true)
                .data(service.myNotifications())
                .build();
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> unreadCount() {
        return ApiResponse.builder()
                .success(true)
                .data(service.unreadCount())
                .build();
    }

    @GetMapping("/preferences")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> preferences() {
        return ApiResponse.builder()
                .success(true)
                .data(service.myPreferences())
                .build();
    }

    @PutMapping("/preferences")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> updatePreferences(@RequestBody Map<String, Boolean> body) {
        return ApiResponse.builder()
                .success(true)
                .message("Notification preferences updated")
                .data(service.updatePreferences(body.get("notificationsEnabled")))
                .build();
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> markRead(@PathVariable Long id) {
        service.markRead(id);

        return ApiResponse.builder()
                .success(true)
                .message("Notification marked as read")
                .build();
    }
}