package com.vidhuratech.jobs.lms.course.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.lms.course.dto.CourseResponseDTO;
import com.vidhuratech.jobs.lms.course.dto.CourseSearchFilterDTO;
import com.vidhuratech.jobs.lms.course.entity.CourseStatus;
import com.vidhuratech.jobs.lms.course.repository.CourseRepository;
import com.vidhuratech.jobs.lms.course.service.CourseService;
import com.vidhuratech.jobs.trainer.service.TrainerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/courses")
@RequiredArgsConstructor
public class PublicCourseController {

    private final CourseService service;
    private final CourseRepository courseRepository;
    private final TrainerDashboardService trainerDashboardService;

    @GetMapping("/{courseId}/curriculum")
    public ApiResponse<?> getCourseCurriculum(@PathVariable Long courseId) {
        return ApiResponse.builder()
                .success(true)
                .data(trainerDashboardService.getOrCreateCourseCurriculum(courseId))
                .build();
    }

    @GetMapping
    public ApiResponse<List<CourseResponseDTO>> getActiveCourses(
            @RequestParam(required = false) Boolean preview
    ) {
        boolean isPreview = preview != null && preview;
        List<CourseResponseDTO> courses = service.getActiveCourses(isPreview);

        return ApiResponse.<List<CourseResponseDTO>>builder()
                .success(true)
                .data(courses)
                .build();
    }

    @GetMapping("/featured")
    public ApiResponse<List<CourseResponseDTO>> getFeaturedCourses() {

        List<CourseResponseDTO> courses =
                service.getFeaturedCourses();

        return ApiResponse.<List<CourseResponseDTO>>builder()
                .success(true)
                .data(courses)
                .build();
    }
}