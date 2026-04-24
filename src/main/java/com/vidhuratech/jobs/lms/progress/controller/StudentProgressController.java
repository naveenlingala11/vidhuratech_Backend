package com.vidhuratech.jobs.lms.progress.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.lms.progress.service.StudentProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/progress")
@RequiredArgsConstructor
public class StudentProgressController {

    private final StudentProgressService service;

    @PostMapping("/complete")
    public ApiResponse<?> markCompleted(
            @RequestParam Long batchId,
            @RequestParam Long sessionId
    ) {
        service.markCompleted(batchId, sessionId);
        return ApiResponse.builder().success(true).build();
    }

    @PostMapping("/resume")
    public ApiResponse<?> updateLastWatched(
            @RequestParam Long batchId,
            @RequestParam Long sessionId
    ) {
        service.updateLastWatched(batchId, sessionId);
        return ApiResponse.builder().success(true).build();
    }

    @GetMapping("/batches/{batchId}/progress")
    public int progress(@PathVariable Long batchId) {
        return service.getProgress(batchId);
    }

    @GetMapping("/batches/{batchId}/resume")
    public ApiResponse<?> resume(@PathVariable Long batchId) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getResumeData(batchId))
                .build();
    }

    @PostMapping("/resume-time")
    public ApiResponse<?> updateResumeWithTime(
            @RequestParam Long batchId,
            @RequestParam Long sessionId,
            @RequestParam Long time
    ) {
        service.updateResumeWithTime(batchId, sessionId, time);
        return ApiResponse.builder().success(true).build();
    }
}