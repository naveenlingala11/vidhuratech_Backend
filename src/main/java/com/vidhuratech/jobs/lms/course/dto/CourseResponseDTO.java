package com.vidhuratech.jobs.lms.course.dto;

import com.vidhuratech.jobs.lms.course.entity.CourseLevel;
import com.vidhuratech.jobs.lms.course.entity.CourseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CourseResponseDTO {

    private Long id;
    private String title;
    private String code;
    private String description;
    private String thumbnailUrl;
    private CourseLevel level;
    private CourseStatus status;
    private Integer durationHours;
    private Boolean active;
    private LocalDateTime createdAt;
    private Double price;

    private String metadataJson;
}