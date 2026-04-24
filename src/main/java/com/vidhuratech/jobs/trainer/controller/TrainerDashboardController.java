package com.vidhuratech.jobs.trainer.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.trainer.service.TrainerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
public class TrainerDashboardController {

    private final TrainerDashboardService service;

    // ✅ DASHBOARD
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> getDashboard() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getDashboard())
                .build();
    }

    // ✅ BATCHES
    @GetMapping("/batches")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> getBatches() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getBatches())
                .build();
    }

    // ✅ STUDENTS
    @GetMapping("/students")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> getStudents() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getStudents())
                .build();
    }

    // ✅ UPLOAD CURRICULUM
    @PostMapping("/upload-curriculum")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<?> uploadCurriculum(
            @RequestParam MultipartFile file,
            @RequestParam Long batchId
    ) throws Exception {

        String content = new String(file.getBytes());

        service.saveOrUpdateCurriculum(batchId, content);

        return ResponseEntity.ok("Uploaded");
    }

    // ✅ GET CURRICULUM
    @GetMapping("/curriculum")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> getCurriculum(@RequestParam Long batchId) {

        return ApiResponse.builder()
                .success(true)
                .data(service.getCurriculum(batchId).orElse(null))
                .build();
    }
}