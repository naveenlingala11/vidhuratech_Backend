package com.vidhuratech.jobs.lms.student.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.lms.student.service.StudentLmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/lms")
@RequiredArgsConstructor
public class StudentLmsController {

    private final StudentLmsService service;

    @GetMapping("/batches")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> myBatches() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getMyBatches())
                .build();
    }

    @GetMapping("/batches/{batchId}/sessions")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> sessions(@PathVariable Long batchId) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getBatchSessions(batchId))
                .build();
    }

    @GetMapping("/batches/{batchId}/curriculum")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> curriculum(@PathVariable Long batchId) {

        return ApiResponse.builder()
                .success(true)
                .data(service.getCurriculum(batchId))
                .build();
    }

    @GetMapping("/public/curriculum")
    public ApiResponse<?> preview(@RequestParam Long batchId) {

        return ApiResponse.builder()
                .success(true)
                .data(
                        service.getCurriculumPreview(batchId)
                )
                .build();
    }
}