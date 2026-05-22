package com.vidhuratech.jobs.student.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.student.service.StudentPseudoCodeChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student/pseudo-challenges")
@RequiredArgsConstructor
public class StudentPseudoCodeChallengeController {

    private final StudentPseudoCodeChallengeService service;

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getStudentChallenges())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> details(@PathVariable Long id) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getChallenge(id))
                .build();
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<?> submit(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Code submitted and evaluated successfully")
                .data(service.submitChallenge(id, payload))
                .build();
    }

    @PostMapping("/{id}/run")
    public ApiResponse<?> run(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Code executed successfully")
                .data(service.runChallenge(id, payload))
                .build();
    }

    @PostMapping("/{id}/save")
    public ApiResponse<?> save(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Code saved successfully")
                .data(service.saveDraft(id, payload))
                .build();
    }
}