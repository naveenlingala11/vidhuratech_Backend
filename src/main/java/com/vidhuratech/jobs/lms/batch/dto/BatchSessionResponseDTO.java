package com.vidhuratech.jobs.lms.batch.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BatchSessionResponseDTO {

    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private Integer durationMinutes;
    private LocalDate sessionDate;
    private Boolean published;
}