package com.vidhuratech.jobs.mentor.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.mentor.dto.MentorVerificationRequest;
import com.vidhuratech.jobs.mentor.service.MentorProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/mentors")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminMentorController {

    private final MentorProfileService service;

    @GetMapping
    public ApiResponse<?> listAllMentors() {
        return ApiResponse.success(service.getAllMentors());
    }

    @PostMapping("/{userId}")
    public ApiResponse<?> promoteToMentor(@PathVariable Long userId) {
        return ApiResponse.success(service.promoteToMentor(userId), "User promoted to mentor successfully");
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<?> demoteFromMentor(@PathVariable Long userId) {
        service.demoteFromMentor(userId);
        return ApiResponse.success(null, "Mentor demoted/removed successfully");
    }

    @PutMapping("/{userId}/status")
    public ApiResponse<?> updateStatus(
            @PathVariable Long userId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean featured
    ) {
        if (active != null) {
            service.toggleActiveStatus(userId, active);
        }
        if (featured != null) {
            service.toggleFeaturedStatus(userId, featured);
        }
        return ApiResponse.success(null, "Mentor status updated successfully");
    }

    @PutMapping("/{userId}/verification")
    public ApiResponse<?> updateVerification(
            @PathVariable Long userId,
            @RequestBody MentorVerificationRequest request
    ) {
        return ApiResponse.success(
                service.updateVerification(
                        userId,
                        request.getIdentityVerified(),
                        request.getCompanyVerified(),
                        request.getLinkedinVerified(),
                        request.getCertVerified(),
                        request.getTermsVerified(),
                        request.getVerificationDocumentUrl()
                ),
                "Verification checklist updated successfully"
        );
    }
}
