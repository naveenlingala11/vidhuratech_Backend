package com.vidhuratech.jobs.lms.course.dto;

import com.vidhuratech.jobs.lms.course.entity.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CourseRequestDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String code;

    private String description;
    
    @NotNull
    private CourseLevel level;

    private Integer durationHours;

    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull
    private Double price;

    private String metadataJson;

    private Boolean featuredOnHome;

    private Integer featuredRank;

    private Boolean autoMonthlyBatchEnabled;

    private Integer monthlyBatchDurationMonths;

    private Long defaultTrainerId;

}