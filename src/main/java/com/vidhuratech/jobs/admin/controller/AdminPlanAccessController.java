package com.vidhuratech.jobs.admin.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.admin.service.AdminPlanAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/plan-access")
@RequiredArgsConstructor
@CrossOrigin
public class AdminPlanAccessController {

    private final AdminPlanAccessService service;

    @GetMapping
    public ApiResponse<?> list(@RequestParam(required = false) String search) {
        return ApiResponse.builder()
                .success(true)
                .data(service.list(search))
                .build();
    }

    @PostMapping
    public ApiResponse<?> grant(@RequestBody Map<String, Object> payload) {
        return ApiResponse.builder()
                .success(true)
                .message("Access granted successfully")
                .data(service.grant(payload))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Access updated successfully")
                .data(service.update(id, payload))
                .build();
    }

    @PatchMapping("/{id}/revoke")
    public ApiResponse<?> revoke(@PathVariable Long id) {
        service.revoke(id);

        return ApiResponse.builder()
                .success(true)
                .message("Access revoked successfully")
                .build();
    }
}