package com.vidhuratech.jobs.lms.batch.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.lms.batch.dto.*;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lms/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN','SUPER_ADMIN')")
    public ApiResponse<?> getBatch(@PathVariable Long id) {
        return ApiResponse.success(batchService.getBatchById(id));
    }

    @GetMapping("/{id}/sessions")
    @PreAuthorize("hasAnyRole('TRAINER','ADMIN','SUPER_ADMIN')")
    public ApiResponse<?> getSessions(@PathVariable Long id) {
        return ApiResponse.builder()
                .success(true)
                .data(batchService.getSessions(id))
                .build();
    }

    @PostMapping("/{id}/sessions")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> createSession(
            @PathVariable Long id,
            @RequestBody BatchSessionRequestDTO dto
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Session created successfully")
                .data(batchService.createSession(id, dto))
                .build();
    }

    @PatchMapping("/{batchId}/sessions/{sessionId}/publish")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> publish(
            @PathVariable Long batchId,
            @PathVariable Long sessionId
    ) {
        batchService.publishSession(batchId, sessionId);

        return ApiResponse.builder()
                .success(true)
                .message("Session published")
                .build();
    }

    @PatchMapping("/{batchId}/sessions/{sessionId}/unpublish")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> unpublish(
            @PathVariable Long batchId,
            @PathVariable Long sessionId
    ) {
        batchService.unpublishSession(batchId, sessionId);

        return ApiResponse.builder()
                .success(true)
                .message("Session unpublished")
                .build();
    }

    @DeleteMapping("/{batchId}/sessions/{sessionId}")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> delete(
            @PathVariable Long batchId,
            @PathVariable Long sessionId
    ) {
        batchService.deleteSession(batchId, sessionId);

        return ApiResponse.builder()
                .success(true)
                .message("Session deleted")
                .build();
    }

    @PostMapping("/{batchId}/enrollments")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ApiResponse<?> enrollStudent(
            @PathVariable Long batchId,
            @RequestBody BatchEnrollmentRequestDTO dto
    ) {
        batchService.enrollStudent(batchId, dto.getStudentId());

        return ApiResponse.builder()
                .success(true)
                .message("Student enrolled successfully")
                .build();
    }

    @DeleteMapping("/enrollments/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ApiResponse<?> removeEnrollment(
            @PathVariable Long enrollmentId
    ) {
        batchService.removeEnrollment(enrollmentId);

        return ApiResponse.builder()
                .success(true)
                .message("Enrollment removed")
                .build();
    }

    @GetMapping("/{batchId}/enrollments")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ApiResponse<?> getEnrollments(@PathVariable Long batchId) {
        return ApiResponse.builder()
                .success(true)
                .data(batchService.getEnrollments(batchId))
                .build();
    }

    @GetMapping("/course/{courseId}/active")
    public ApiResponse<?> getActiveBatch(@PathVariable Long courseId) {

        Batch batch = batchService.getActiveBatchByCourse(courseId);

        if (batch == null) {
            return ApiResponse.builder()
                    .success(true)
                    .data(null) // 🔥 no error
                    .message("No batch available")
                    .build();
        }

        return ApiResponse.builder()
                .success(true)
                .data(Map.of(
                        "id", batch.getId(),
                        "name", batch.getName(),
                        "startDate", batch.getStartDate()
                ))
                .build();
    }
}