package com.vidhuratech.jobs.lms.batch.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.lms.batch.dto.BatchRequestDTO;
import com.vidhuratech.jobs.lms.batch.service.AdminBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lms/admin/batches")
@RequiredArgsConstructor
public class AdminBatchController {

    private final AdminBatchService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ApiResponse<?> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long trainerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getAllBatches(keyword, status, courseId, trainerId, page, size))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ApiResponse<?> create(
            @RequestBody BatchRequestDTO dto
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Batch created successfully")
                .data(service.createBatch(dto))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ApiResponse<?> update(
            @PathVariable Long id,
            @RequestBody BatchRequestDTO dto
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Batch updated successfully")
                .data(service.updateBatch(id, dto))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ApiResponse<?> delete(@PathVariable Long id) {
        service.deleteBatch(id);

        return ApiResponse.builder()
                .success(true)
                .message("Batch deleted successfully")
                .build();
    }
}