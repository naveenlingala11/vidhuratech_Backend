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
                .code(dto.getCode().toUpperCase().trim())
                .description(dto.getDescription())
                .thumbnailUrl(dto.getThumbnailUrl())
                .level(dto.getLevel())
                .durationHours(dto.getDurationHours())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .price(dto.getPrice())
                .metadataJson(dto.getMetadataJson())
                .status(CourseStatus.DRAFT)
                .active(true)
                .featuredOnHome(Boolean.TRUE.equals(dto.getFeaturedOnHome()))
                .featuredRank(dto.getFeaturedRank() == null ? 100 : dto.getFeaturedRank())
                .autoMonthlyBatchEnabled(Boolean.TRUE.equals(dto.getAutoMonthlyBatchEnabled()))
                .monthlyBatchDurationMonths(
                        dto.getMonthlyBatchDurationMonths() == null
                                ? 3
                                : dto.getMonthlyBatchDurationMonths()
                )
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
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .active(course.getActive())
                .createdAt(course.getCreatedAt())
                .price(course.getPrice())
                .metadataJson(course.getMetadataJson())
                .featuredOnHome(Boolean.TRUE.equals(course.getFeaturedOnHome()))
                .featuredRank(course.getFeaturedRank())
                .autoMonthlyBatchEnabled(Boolean.TRUE.equals(course.getAutoMonthlyBatchEnabled()))
                .monthlyBatchDurationMonths(course.getMonthlyBatchDurationMonths())
                .defaultTrainerId(
                        course.getDefaultTrainer() != null
                                ? course.getDefaultTrainer().getId()
                                : null
                )
                .defaultTrainerName(
                        course.getDefaultTrainer() != null
                                ? course.getDefaultTrainer().getName()
                                : null
                )
                .build();
    }

    public void updateEntity(Course course, CourseRequestDTO dto) {
        course.setTitle(dto.getTitle());
        course.setCode(dto.getCode().toUpperCase().trim());
        course.setDescription(dto.getDescription());
        course.setThumbnailUrl(dto.getThumbnailUrl());
        course.setLevel(dto.getLevel());
        course.setDurationHours(dto.getDurationHours());
        course.setStartDate(dto.getStartDate());
        course.setEndDate(dto.getEndDate());
        course.setPrice(dto.getPrice());
        course.setMetadataJson(dto.getMetadataJson());

        course.setFeaturedOnHome(Boolean.TRUE.equals(dto.getFeaturedOnHome()));
        course.setFeaturedRank(dto.getFeaturedRank() == null ? 100 : dto.getFeaturedRank());

        course.setAutoMonthlyBatchEnabled(Boolean.TRUE.equals(dto.getAutoMonthlyBatchEnabled()));
        course.setMonthlyBatchDurationMonths(
                dto.getMonthlyBatchDurationMonths() == null
                        ? 3
                        : dto.getMonthlyBatchDurationMonths()
        );
    }
}