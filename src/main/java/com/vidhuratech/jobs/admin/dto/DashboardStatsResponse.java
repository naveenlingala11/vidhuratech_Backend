package com.vidhuratech.jobs.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalUsers;
    private long activeUsers;
    private long totalStudents;
    private long trainers;
    private long admins;
    private long mentors;
}