package com.vidhuratech.jobs.lms.course.dto;

import com.vidhuratech.jobs.lms.course.entity.CourseLevel;
import com.vidhuratech.jobs.lms.course.entity.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDTO {

    private Long id;
    private String title;
    private String code;
    private String description;
    private String thumbnailUrl;
    private CourseLevel level;
    private CourseStatus status;
    private Integer durationHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private LocalDateTime createdAt;
    private Double price;

    private String metadataJson;

    private Boolean featuredOnHome;

    private Integer featuredRank;

    private Boolean autoMonthlyBatchEnabled;

    private Integer monthlyBatchDurationMonths;

    private Long defaultTrainerId;
    private String defaultTrainerName;
}