package com.vidhuratech.jobs.student.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class StudentDashboardResponseDTO {

    private Map<String, Object> stats;

    private Map<String, List<?>> sections;
}