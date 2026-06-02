package com.vidhuratech.jobs.admin.controller;

import com.vidhuratech.jobs.admin.service.AdminCommercialControlService;
import com.vidhuratech.jobs.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/commercial-control")
@RequiredArgsConstructor
@CrossOrigin
public class AdminCommercialControlController {

    private final AdminCommercialControlService service;

    @GetMapping("/people")
    public ApiResponse<?> people(@RequestParam(required = false) String search) {
        return ApiResponse.builder().success(true).data(service.people(search)).build();
    }

    @GetMapping("/pricing")
    public ApiResponse<?> pricing() {
        return ApiResponse.builder().success(true).data(service.pricing()).build();
    }

    @PutMapping("/pricing/{planCode}")
    public ApiResponse<?> updatePricing(@PathVariable String planCode, @RequestBody Map<String, Object> payload) {
        return ApiResponse.builder()
                .success(true)
                .message("Pricing updated")
                .data(service.updatePricing(planCode, payload))
                .build();
    }

    @GetMapping("/discounts")
    public ApiResponse<?> discounts() {
        return ApiResponse.builder().success(true).data(service.discounts()).build();
    }

    @PostMapping("/discounts")
    public ApiResponse<?> saveDiscount(@RequestBody Map<String, Object> payload) {
        return ApiResponse.builder()
                .success(true)
                .message("Discount saved")
                .data(service.saveDiscount(payload))
                .build();
    }

    @GetMapping("/project-controls")
    public ApiResponse<?> projectControls() {
        return ApiResponse.builder().success(true).data(service.projectControls()).build();
    }

    @PutMapping("/project-controls/{key}")
    public ApiResponse<?> updateProjectControl(@PathVariable String key, @RequestBody Map<String, Object> payload) {
        return ApiResponse.builder()
                .success(true)
                .message("Project control updated")
                .data(service.updateProjectControl(key, payload))
                .build();
    }
}