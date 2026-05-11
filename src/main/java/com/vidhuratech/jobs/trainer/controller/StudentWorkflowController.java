package com.vidhuratech.jobs.trainer.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.trainer.service.StudentWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentWorkflowController {

    private final StudentWorkflowService service;

    @PostMapping("/mock-interviews")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> requestMockInterview(@RequestBody Map<String, Object> payload) {
        return ApiResponse.builder()
                .success(true)
                .message("Mock interview request sent")
                .data(service.createMockInterviewRequest(payload))
                .build();
    }

    @GetMapping("/mock-interviews")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> myMockInterviews() {
        return ApiResponse.builder().success(true).data(service.getMyMockInterviews()).build();
    }

    @GetMapping("/work-items")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> myWorkItems() {
        return ApiResponse.builder().success(true).data(service.getMyWorkItems()).build();
    }

    @PostMapping("/work-items/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> submitWork(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.builder()
                .success(true)
                .message("Submitted successfully")
                .data(service.submitWork(id, payload))
                .build();
    }
}

