package com.vidhuratech.jobs.mentor.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.mentor.service.StudentMentorService;
import com.vidhuratech.jobs.mentor.service.MentorReviewService;
import com.vidhuratech.jobs.mentor.service.MentorBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/mentors")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasRole('STUDENT')")
public class StudentMentorController {

    private final StudentMentorService service;
    private final MentorReviewService reviewService;
    private final MentorBookingService bookingService;
    private final SecurityUtils securityUtils;

    @GetMapping("/dashboard")
    public ApiResponse<?> getStudentMentorDashboard() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        return ApiResponse.success(service.getStudentMentorDashboard(userId));
    }

    @PostMapping("/reviews")
    public ApiResponse<?> submitReview(@RequestBody ReviewRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        return ApiResponse.success(reviewService.submitReview(
                userId, request.getMentorId(), request.getRating(),
                request.getReviewText(), request.getSessionType()
        ));
    }

    @PostMapping("/booking-requests")
    public ApiResponse<?> createBookingRequest(@RequestBody BookingRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        return ApiResponse.success(bookingService.createBookingRequest(
                userId, request.getMentorId(), request.getTopic(),
                request.getMessage(), request.getPreferredPlan()
        ));
    }

    @GetMapping("/booking-requests")
    public ApiResponse<?> getMyBookingRequests() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        return ApiResponse.success(bookingService.getStudentBookingRequests(userId));
    }

    @lombok.Data
    public static class ReviewRequest {
        private Long mentorId;
        private Integer rating;
        private String reviewText;
        private String sessionType;
    }

    @lombok.Data
    public static class BookingRequest {
        private Long mentorId;
        private String topic;
        private String message;
        private String preferredPlan;
    }
}

