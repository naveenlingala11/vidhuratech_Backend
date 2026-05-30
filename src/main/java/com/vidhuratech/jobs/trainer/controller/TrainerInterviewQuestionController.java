package com.vidhuratech.jobs.trainer.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.trainer.service.TrainerInterviewQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainer/interview-questions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TRAINER')")
public class TrainerInterviewQuestionController {

    private final TrainerInterviewQuestionService service;

    @PostMapping
    public ApiResponse<?> create(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(service.create(payload), "Interview question created");
    }

    @PostMapping("/bulk")
    public ApiResponse<?> bulk(@RequestBody List<Map<String, Object>> payload) {
        return ApiResponse.success(service.bulkCreate(payload), "Interview questions processed");
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(service.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<?> details(@PathVariable Long id) {
        return ApiResponse.success(service.details(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.success(service.update(id, payload), "Interview question updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null, "Interview question deleted");
    }
}