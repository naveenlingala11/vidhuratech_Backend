package com.vidhuratech.jobs.trainer.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.trainer.service.TrainerPseudoCodeChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/trainer/pseudo-challenges")
@RequiredArgsConstructor
public class TrainerPseudoCodeChallengeController {

    private final TrainerPseudoCodeChallengeService service;

    @PostMapping
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> create(@RequestBody Map<String, Object> payload) {
        return ApiResponse.builder()
                .success(true)
                .message("Pseudo code challenge created successfully")
                .data(service.createChallenge(payload))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> list() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getTrainerChallenges())
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> details(@PathVariable Long id) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getChallengeDetails(id))
                .build();
    }

    @GetMapping("/{id}/attempts")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> attempts(@PathVariable Long id) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getAttempts(id))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> delete(@PathVariable Long id) {
        service.deleteChallenge(id);

        return ApiResponse.builder()
                .success(true)
                .message("Pseudo code challenge deleted")
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.builder()
                .success(true)
                .message("Challenge updated successfully")
                .data(service.updateChallenge(id, payload))
                .build();
    }
}