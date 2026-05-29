package com.vidhuratech.jobs.publicpractice.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.publicpractice.service.PublicPracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/practice")
@RequiredArgsConstructor
@CrossOrigin
public class PublicPracticeController {

    private final PublicPracticeService service;

    @GetMapping
    public ApiResponse<?> getPracticeLibrary(
            @RequestParam(required = false) String company
    ) {
        Object data = company == null || company.isBlank()
                ? service.getPracticeLibrary()
                : service.getPracticeLibraryByCompany(company);

        return ApiResponse.builder()
                .success(true)
                .data(data)
                .build();
    }

    @PostMapping("/lead")
    public ApiResponse<?> savePracticeLead(
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Registration completed successfully")
                .data(service.savePracticeLead(payload))
                .build();
    }

    @GetMapping("/assessments/{id}")
    public ApiResponse<?> getAssessment(@PathVariable Long id) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getPublicAssessment(id))
                .build();
    }

    @PostMapping("/assessments/{id}/submit")
    public ApiResponse<?> submitAssessment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Mock test submitted successfully")
                .data(service.submitPublicAssessment(id, payload))
                .build();
    }

    @GetMapping("/challenges/{id}")
    public ApiResponse<?> getChallenge(@PathVariable Long id) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getPublicChallenge(id))
                .build();
    }

    @PostMapping("/challenges/{id}/run")
    public ApiResponse<?> runChallenge(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Challenge evaluated successfully")
                .data(service.runPublicChallenge(id, payload))
                .build();
    }

    @PostMapping("/access/register")
    public ApiResponse<?> registerAccess(
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Registration completed successfully")
                .data(service.registerPracticeAccess(payload))
                .build();
    }
}