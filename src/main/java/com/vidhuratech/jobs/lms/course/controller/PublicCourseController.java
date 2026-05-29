package com.vidhuratech.jobs.lms.course.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.lms.course.dto.CourseResponseDTO;
import com.vidhuratech.jobs.lms.course.dto.CourseSearchFilterDTO;
import com.vidhuratech.jobs.lms.course.entity.CourseStatus;
import com.vidhuratech.jobs.lms.course.repository.CourseRepository;
import com.vidhuratech.jobs.lms.course.service.CourseService;
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

    @GetMapping
    public ApiResponse<List<CourseResponseDTO>> getActiveCourses(
            @RequestParam(required = false) Boolean preview
    ) {

        CourseSearchFilterDTO filter = new CourseSearchFilterDTO();
        filter.setActive(true);

        if (preview == null || !preview) {
            filter.setStatus(CourseStatus.PUBLISHED);
        }

        List<CourseResponseDTO> courses = service
                .search(filter, PageRequest.of(0, 100))
                .getContent();

        return ApiResponse.<List<CourseResponseDTO>>builder()
                .success(true)
                .data(courses)
                .build();
    }


    @GetMapping("/featured")
    public ApiResponse<List<CourseResponseDTO>> getFeaturedCourses() {
        List<CourseResponseDTO> courses = service.getFeaturedCourses();

        return ApiResponse.<List<CourseResponseDTO>>builder()
                .success(true)
                .data(courses)
                .build();
    }

    @GetMapping("/featured")
    public ApiResponse<List<CourseResponseDTO>> featuredCourses() {
        return ApiResponse.<List<CourseResponseDTO>>builder()
                .success(true)
                .message("Featured courses fetched successfully")
                .data(service.getFeaturedCourses())
                .build();
    }
}