package com.vidhuratech.jobs.trainer.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.trainer.service.TrainerAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/trainer/assessments")
@RequiredArgsConstructor
@CrossOrigin
public class TrainerAssessmentController {

    private final TrainerAssessmentService service;

    @PostMapping
    public ApiResponse<?> createAssessment(@RequestBody Map<String, Object> payload) {
        return ApiResponse.builder()
                .success(true)
                .message("Assessment created successfully")
                .data(service.createAssessment(payload))
                .build();
    }

    @PostMapping("/bulk")
    public ApiResponse<?> bulkUploadAssessments(@RequestBody Map<String, Object> payload) {
        return ApiResponse.builder()
                .success(true)
                .message("Bulk assessment uploaded successfully")
                .data(service.createAssessment(payload))
                .build();
    }

    @GetMapping
    public ApiResponse<?> getTrainerAssessments() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getTrainerAssessments())
                .build();
    }

    @GetMapping("/{assessmentId}")
    public ApiResponse<?> getAssessmentDetails(@PathVariable Long assessmentId) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getAssessmentDetails(assessmentId))
                .build();
    }

    @GetMapping("/{assessmentId}/attempts")
    public ApiResponse<?> getAttempts(@PathVariable Long assessmentId) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getAssessmentAttempts(assessmentId))
                .build();
    }

    @GetMapping("/{assessmentId}/attempts/{attemptId}")
    public ApiResponse<?> getAttemptDetails(
            @PathVariable Long assessmentId,
            @PathVariable Long attemptId
    ) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getAssessmentAttemptDetails(assessmentId, attemptId))
                .build();
    }

    @DeleteMapping("/{assessmentId}")
    public ApiResponse<?> deleteAssessment(@PathVariable Long assessmentId) {
        service.deleteAssessment(assessmentId);

        return ApiResponse.builder()
                .success(true)
                .message("Assessment deleted")
                .build();
    }
}