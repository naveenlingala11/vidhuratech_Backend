package com.vidhuratech.jobs.lms.batch.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BatchResponseDTO {

    private Long id;
    private String name;

    private String courseName;
    private String trainerName;

    private LocalDate startDate;
    private LocalDate endDate;

    private String status;
}