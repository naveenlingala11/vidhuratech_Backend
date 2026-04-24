package com.vidhuratech.jobs.lms.batch.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BatchSessionRequestDTO {

    private String title;
    private String description;
    private String videoUrl;
    private Integer durationMinutes;
    private LocalDate sessionDate;
    private Boolean published;
}