package com.vidhuratech.jobs.lms.batch.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.batch.dto.*;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
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
    private final BatchEnrollmentRepository enrollmentRepository;
    private final SecurityUtils securityUtils;

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

    @GetMapping("/{batchId}/is-enrolled")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> isEnrolled(@PathVariable Long batchId) {

        Long userId = securityUtils.getCurrentUserId();

        boolean enrolled =
                enrollmentRepository.existsByBatchIdAndStudentId(batchId, userId);

        return ApiResponse.success(enrolled);
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

        try {

            Batch batch = batchService.getActiveBatchByCourse(courseId);

            if (batch == null) {
                return ApiResponse.builder()
                        .success(true)
                        .data(null)
                        .message("No batch available")
                        .build();
            }

            return ApiResponse.builder()
                    .success(true)
                    .data(Map.of(
                            "id", batch.getId(),
                            "name", batch.getName() != null ? batch.getName() : "",
                            "startDate", batch.getStartDate(),
                            "status", batch.getStatus() != null ? batch.getStatus().name() : "UPCOMING",

                            // 🔥 SAFE NULL HANDLING
                            "courseName", batch.getCourse() != null ? batch.getCourse().getTitle() : "",
                            "trainerName", batch.getTrainer() != null ? batch.getTrainer().getName() : ""
                    ))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();

            return ApiResponse.builder()
                    .success(false)
                    .message("Failed to fetch batch")
                    .build();
        }
    }
}