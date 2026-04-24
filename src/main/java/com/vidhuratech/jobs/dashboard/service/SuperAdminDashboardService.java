package com.vidhuratech.jobs.dashboard.service;

import com.vidhuratech.jobs.dashboard.dto.DashboardStatsResponse;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SuperAdminDashboardService {

    private final UserRepository userRepository;

    public DashboardStatsResponse getDashboard() {

        long totalUsers = userRepository.count();

        long activeUsers = userRepository.countByActiveTrue();

        long inactiveUsers = userRepository.countByActiveFalse();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", inactiveUsers);

        return DashboardStatsResponse.builder()
                .stats(stats)
                .sections(Map.of())
                .build();
    }
}