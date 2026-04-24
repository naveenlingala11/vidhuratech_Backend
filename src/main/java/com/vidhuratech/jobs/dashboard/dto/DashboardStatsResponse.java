package com.vidhuratech.jobs.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.List;

@Data
@Builder
public class DashboardStatsResponse {

    private Map<String, Object> stats;

    private Map<String, List<?>> sections;
}