package com.vidhuratech.jobs.trainer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainerStatsDTO {

    private Long assignedBatches;
    private Long totalStudents;
    private Long pendingReviews;
    private Long todaysSessions;
    private Double avgAttendance;
    private Long assignmentsSubmitted;
}