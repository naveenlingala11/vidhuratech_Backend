package com.vidhuratech.jobs.mentor.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.mentor.dto.MentorProfileRequest;
import com.vidhuratech.jobs.mentor.dto.MentorProfileResponse;
import com.vidhuratech.jobs.mentor.service.MentorProfileService;
import com.vidhuratech.jobs.mentor.service.MentorBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentor/profile")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasRole('MENTOR')")
public class MentorProfileController {

    private final MentorProfileService service;
    private final MentorBookingService bookingService;
    private final SecurityUtils securityUtils;

    @GetMapping("/booking-requests")
    public ApiResponse<?> getBookingRequests() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        return ApiResponse.success(bookingService.getMentorBookingRequests(userId));
    }

    @PostMapping("/booking-requests/{id}/accept")
    public ApiResponse<?> acceptBookingRequest(
            @PathVariable Long id,
            @RequestBody ActionRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        return ApiResponse.success(bookingService.acceptBookingRequest(userId, id, request.getNote()));
    }

    @PostMapping("/booking-requests/{id}/reject")
    public ApiResponse<?> rejectBookingRequest(
            @PathVariable Long id,
            @RequestBody ActionRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        return ApiResponse.success(bookingService.rejectBookingRequest(userId, id, request.getNote()));
    }

    @lombok.Data
    public static class ActionRequest {
        private String note;
    }

    @GetMapping
    public ApiResponse<?> getProfile() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        return ApiResponse.success(service.getOrCreateProfile(userId));
    }

    @GetMapping("/dashboard")
    public ApiResponse<?> getDashboardData() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        return ApiResponse.success(service.getDashboardData(userId));
    }

    @PutMapping
    public ApiResponse<?> updateProfile(
            @RequestBody MentorProfileRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        return ApiResponse.success(service.updateProfile(userId, request), "Profile updated successfully");
    }

    @PostMapping("/availability")
    public ApiResponse<?> saveAvailability(
            @RequestBody AvailabilityRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        service.saveAvailability(userId, request.getDays(), request.getSlots(), request.getAllowDaily());
        return ApiResponse.success(null, "Availability saved successfully");
    }

    @PostMapping("/sessions")
    public ApiResponse<?> scheduleSession(
            @RequestBody SessionRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        service.scheduleSession(userId, request.getStudentName(), request.getDate(), request.getTime(), request.getType(), request.getLink());
        return ApiResponse.success(null, "Session scheduled successfully");
    }

    @PostMapping("/feedback")
    public ApiResponse<?> submitFeedback(
            @RequestBody FeedbackRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        service.submitFeedback(userId, request.getStudentName(), request.getProgress(), request.getMilestone(), request.getNote());
        return ApiResponse.success(null, "Feedback saved successfully");
    }

    @lombok.Data
    public static class AvailabilityRequest {
        private String days;
        private String slots;
        private Boolean allowDaily;
    }

    @lombok.Data
    public static class SessionRequest {
        private String studentName;
        private String date;
        private String time;
        private String type;
        private String link;
    }

    @lombok.Data
    public static class FeedbackRequest {
        private String studentName;
        private Integer progress;
        private String milestone;
        private String note;
    }
}
