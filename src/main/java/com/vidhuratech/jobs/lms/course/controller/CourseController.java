package com.vidhuratech.jobs.lms.course.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.lms.course.dto.*;
import com.vidhuratech.jobs.lms.course.service.CourseService;
import com.vidhuratech.jobs.lms.course.service.CourseThumbnailStorageService;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lms/courses") // ✅ IMPORTANT
@RequiredArgsConstructor
public class CourseController {

    private final CourseService service;
    private final CourseThumbnailStorageService thumbnailStorageService;

    // ================= CREATE =================
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

    // ================= BULK UPLOAD =================
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<BulkCourseResponse> bulkCreate(
            @RequestBody List<CourseRequestDTO> courses
    ) {
        return ApiResponse.<BulkCourseResponse>builder()
                .success(true)
                .message("Bulk upload completed")
                .data(service.bulkCreate(courses))
                .build();
    }

    // ================= UPDATE =================
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

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TRAINER','MENTOR','STUDENT')")
    public ApiResponse<CourseResponseDTO> getById(@PathVariable Long id) {
        return ApiResponse.<CourseResponseDTO>builder()
                .success(true)
                .data(service.getById(id))
                .build();
    }

    // ================= SEARCH =================
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

    // ================= PUBLISH =================
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Void> publish(@PathVariable Long id) {
        service.publish(id);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Course published successfully")
                .build();
    }

    // ================= UNPUBLISH ===============
    @PatchMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<CourseResponseDTO> unpublish(@PathVariable Long id) {
        return ApiResponse.<CourseResponseDTO>builder()
                .success(true)
                .message("Course unpublished successfully")
                .data(service.unpublish(id))
                .build();
    }

    // ================= ARCHIVE =================
    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Void> archive(@PathVariable Long id) {
        service.archive(id);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Course archived successfully")
                .build();
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.softDelete(id);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Course deleted successfully")
                .build();
    }

    @PostMapping("/{id}/thumbnail")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TRAINER')")
    public ApiResponse<CourseResponseDTO> uploadThumbnail(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        String thumbnailUrl = thumbnailStorageService.store(file);

        return ApiResponse.<CourseResponseDTO>builder()
                .success(true)
                .message("Course thumbnail uploaded successfully")
                .data(service.updateThumbnail(id, thumbnailUrl))
                .build();
    }
}