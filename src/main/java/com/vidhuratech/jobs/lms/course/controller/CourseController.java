package com.vidhuratech.jobs.lms.course.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.lms.course.dto.*;
import com.vidhuratech.jobs.lms.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lms/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TRAINER')")
    public ApiResponse<CourseResponseDTO> create(
            @Valid @RequestBody CourseRequestDTO dto
    ) {
        return ApiResponse.<CourseResponseDTO>builder()
                .success(true)
                .message("Course created successfully")
                .data(service.create(dto))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TRAINER')")
    public ApiResponse<CourseResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequestDTO dto
    ) {
        return ApiResponse.<CourseResponseDTO>builder()
                .success(true)
                .message("Course updated successfully")
                .data(service.update(id, dto))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TRAINER','MENTOR','STUDENT')")
    public ApiResponse<CourseResponseDTO> getById(@PathVariable Long id) {
        return ApiResponse.<CourseResponseDTO>builder()
                .success(true)
                .data(service.getById(id))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TRAINER','MENTOR')")
    public ApiResponse<Page<CourseResponseDTO>> search(
            CourseSearchFilterDTO filter,
            Pageable pageable
    ) {
        return ApiResponse.<Page<CourseResponseDTO>>builder()
                .success(true)
                .data(service.search(filter, pageable))
                .build();
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Void> publish(@PathVariable Long id) {
        service.publish(id);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Course published successfully")
                .build();
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Void> archive(@PathVariable Long id) {
        service.archive(id);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Course archived successfully")
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.softDelete(id);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Course deleted successfully")
                .build();
    }
}