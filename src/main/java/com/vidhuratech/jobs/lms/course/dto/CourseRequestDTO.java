package com.vidhuratech.jobs.lms.course.dto;

import com.vidhuratech.jobs.lms.course.entity.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseRequestDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String code;

    private String description;

    private String thumbnailUrl;

    @NotNull
    private CourseLevel level;

    private Integer durationHours;

    @NotNull
    private Double price;

    private String metadataJson;
}