package com.vidhuratech.jobs.admin.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.lms.batch.service.MonthlyBatchGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/monthly-batches")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminMonthlyBatchController {

    private final MonthlyBatchGenerationService service;

    @PostMapping("/generate-current")
    public ApiResponse<?> generateCurrentMonth() {
        return ApiResponse.success(
                service.createMonthlyBatchesForCurrentMonth(),
                "Monthly batches generated"
        );
    }

    @PostMapping("/generate-next")
    public ApiResponse<?> generateNextMonth() {
        return ApiResponse.success(
                service.createMonthlyBatchesForNextMonth(),
                "Next month batches generated"
        );
    }
}