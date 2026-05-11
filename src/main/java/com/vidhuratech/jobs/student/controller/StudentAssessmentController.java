package com.vidhuratech.jobs.student.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.student.service.StudentAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student/assessments")
@RequiredArgsConstructor
@CrossOrigin
public class StudentAssessmentController {

    private final StudentAssessmentService service;

    @GetMapping
    public ApiResponse<?> getAssessments() {

        return ApiResponse.builder()
                .success(true)
                .data(service.getStudentAssessments())
                .build();
    }

    @GetMapping("/{assessmentId}")
    public ApiResponse<?> getAssessment(
            @PathVariable Long assessmentId
    ) {

        return ApiResponse.builder()
                .success(true)
                .data(service.getAssessmentById(assessmentId))
                .build();
    }

    @PostMapping("/{assessmentId}/submit")
    public ApiResponse<?> submitAssessment(
            @PathVariable Long assessmentId,
            @RequestBody Map<String, Object> payload
    ) {

        return ApiResponse.builder()
                .success(true)
                .message("Assessment submitted")
                .data(service.submitAssessment(assessmentId, payload))
                .build();
    }
}