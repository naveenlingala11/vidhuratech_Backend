package com.vidhuratech.jobs.student.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.student.service.StudentInterviewQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/interview-questions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentInterviewQuestionController {

    private final StudentInterviewQuestionService service;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> myQuestions(
            @RequestParam(defaultValue = "") String company,
            @RequestParam(defaultValue = "") String role,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "") String difficulty,
            @RequestParam(defaultValue = "") String topic,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ApiResponse.builder()
                .success(true)
                .data(service.myInterviewQuestions(company, role, search, type, difficulty, topic, page))
                .build();
    }
}