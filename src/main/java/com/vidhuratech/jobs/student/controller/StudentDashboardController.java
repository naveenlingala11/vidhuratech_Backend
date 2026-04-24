package com.vidhuratech.jobs.student.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.student.service.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentDashboardService service;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> getDashboard() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getDashboard())
                .build();
    }

    @GetMapping("/courses")
    public ApiResponse<?> getCourses() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getMyCourses())
                .build();
    }

    @GetMapping("/assignments")
    public ApiResponse<?> getAssignments() {
        return ApiResponse.builder()
                .success(true)
                .data(List.of())
                .build();
    }

    @GetMapping("/certificates")
    public ApiResponse<?> getCertificates() {
        return ApiResponse.builder()
                .success(true)
                .data(List.of())
                .build();
    }
}