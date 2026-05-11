package com.vidhuratech.jobs.admission.controller;

import com.vidhuratech.jobs.admission.dto.AdmissionResponseDTO;
import com.vidhuratech.jobs.admission.dto.ManualAdmissionRequest;
import com.vidhuratech.jobs.admission.service.AdmissionService;
import com.vidhuratech.jobs.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/admissions")
@RequiredArgsConstructor
public class AdmissionController {

    private final AdmissionService admissionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ResponseEntity<?> create(
            @RequestBody ManualAdmissionRequest request
    ) {

        AdmissionResponseDTO response =
                admissionService.createManualAdmission(request);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Admission created successfully")
                        .data(response)
                        .build()
        );
    }
}