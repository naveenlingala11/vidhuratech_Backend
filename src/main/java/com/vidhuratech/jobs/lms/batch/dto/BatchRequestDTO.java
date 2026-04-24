package com.vidhuratech.jobs.lms.batch.dto;

import com.vidhuratech.jobs.lms.batch.entity.BatchStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BatchRequestDTO {

    private String name;
    private Long courseId;
    private Long trainerId;

    private LocalDate startDate;
    private LocalDate endDate;

    private BatchStatus status;
}