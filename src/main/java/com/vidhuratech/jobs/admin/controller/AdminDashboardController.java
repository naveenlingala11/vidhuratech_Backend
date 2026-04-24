package com.vidhuratech.jobs.admin.controller;

import com.vidhuratech.jobs.admin.dto.DashboardStatsResponse;
import com.vidhuratech.jobs.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/super-admin/dashboard")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService service;

    @GetMapping("/stats")
    public DashboardStatsResponse stats() {
        return service.getStats();
    }
}