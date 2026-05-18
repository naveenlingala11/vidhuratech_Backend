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

    @GetMapping("/{id}")
    public ApiResponse<?> getAssessment(
            @PathVariable Long id
    ) {

        return ApiResponse.builder()
                .success(true)
                .data(service.getAssessment(id))
                .build();
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<?> submitAssessment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {

        return ApiResponse.builder()
                .success(true)
                .message("Assessment submitted successfully")
                .data(
                        service.submitAssessment(
                                id,
                                payload
                        )
                )
                .build();
    }
}