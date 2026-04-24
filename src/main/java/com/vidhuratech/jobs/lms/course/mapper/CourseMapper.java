package com.vidhuratech.jobs.lms.course.mapper;

import com.vidhuratech.jobs.lms.course.dto.CourseRequestDTO;
import com.vidhuratech.jobs.lms.course.dto.CourseResponseDTO;
import com.vidhuratech.jobs.lms.course.entity.Course;
import com.vidhuratech.jobs.lms.course.entity.CourseStatus;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public Course toEntity(CourseRequestDTO dto) {
        return Course.builder()
                .title(dto.getTitle())
                .code(dto.getCode().toUpperCase())
                .description(dto.getDescription())
                .thumbnailUrl(dto.getThumbnailUrl())
                .level(dto.getLevel())
                .durationHours(dto.getDurationHours())
                .status(CourseStatus.DRAFT)
                .active(true)
                .build();
    }

    public CourseResponseDTO toResponse(Course course) {
        return CourseResponseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .code(course.getCode())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .level(course.getLevel())
                .status(course.getStatus())
                .durationHours(course.getDurationHours())
                .active(course.getActive())
                .createdAt(course.getCreatedAt())
                .build();
    }

    public void updateEntity(Course course, CourseRequestDTO dto) {
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setThumbnailUrl(dto.getThumbnailUrl());
        course.setLevel(dto.getLevel());
        course.setDurationHours(dto.getDurationHours());
    }
}