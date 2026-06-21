package com.vidhuratech.jobs.mentor.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.mentor.dto.MentorApplicationRequest;
import com.vidhuratech.jobs.mentor.dto.MentorProfileResponse;
import com.vidhuratech.jobs.mentor.service.MentorProfileService;
import com.vidhuratech.jobs.mentor.service.MentorReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/mentors")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PublicMentorController {

    private final MentorProfileService service;
    private final MentorReviewService reviewService;

    @GetMapping
    public ApiResponse<List<MentorProfileResponse>> getActiveMentors(
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(service.getActiveMentors(search));
    }

    @GetMapping("/{userId}")
    public ApiResponse<MentorProfileResponse> getMentorProfileById(
            @PathVariable Long userId
    ) {
        return ApiResponse.success(service.getMentorProfileById(userId));
    }

    @GetMapping("/{userId}/reviews")
    public ApiResponse<?> getMentorReviews(@PathVariable Long userId) {
        return ApiResponse.success(reviewService.getReviewsForMentor(userId));
    }

    @PostMapping("/apply")
    public ApiResponse<MentorProfileResponse> applyAsMentor(
            @RequestBody MentorApplicationRequest req
    ) {
        return ApiResponse.success(service.applyAsMentor(req));
    }
}
