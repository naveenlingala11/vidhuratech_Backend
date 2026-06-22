package com.vidhuratech.jobs.trainer.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.trainer.service.TrainerWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
@CrossOrigin
public class TrainerWorkflowController {

    private final TrainerWorkflowService service;

    @GetMapping("/mock-interviews")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN', 'SUPER_ADMIN', 'HR', 'MANAGER', 'MENTOR')")
    public ApiResponse<?> getMockInterviewRequests() {
        return ApiResponse.builder().success(true).data(service.getMockInterviewRequests()).build();
    }

    @PatchMapping("/mock-interviews/{id}")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN', 'SUPER_ADMIN', 'HR', 'MANAGER', 'MENTOR')")
    public ApiResponse<?> updateMockInterview(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.builder()
                .success(true)
                .message("Mock interview updated")
                .data(service.updateMockInterview(id, payload))
                .build();
    }

    @PostMapping("/work-items")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> createWorkItem(@RequestBody Map<String, Object> payload) {
        return ApiResponse.builder()
                .success(true)
                .message("Work item created")
                .data(service.createWorkItem(payload))
                .build();
    }

    @GetMapping("/work-items")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> getWorkItems() {
        return ApiResponse.builder().success(true).data(service.getWorkItems()).build();
    }

    @GetMapping("/submissions")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> getSubmissions() {
        return ApiResponse.builder().success(true).data(service.getSubmissions()).build();
    }

    @PatchMapping("/submissions/{id}/review")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> reviewSubmission(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.builder()
                .success(true)
                .message("Submission reviewed")
                .data(service.reviewSubmission(id, payload))
                .build();
    }
}

