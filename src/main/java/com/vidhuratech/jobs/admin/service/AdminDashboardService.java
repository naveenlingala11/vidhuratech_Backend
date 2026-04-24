package com.vidhuratech.jobs.admin.service;

import com.vidhuratech.jobs.admin.dto.DashboardStatsResponse;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepo;

    public DashboardStatsResponse getStats() {
        return new DashboardStatsResponse(
                userRepo.count(),
                userRepo.countByActiveTrue(),
                userRepo.countByRole(UserRole.STUDENT),
                userRepo.countByRole(UserRole.TRAINER),
                userRepo.countByRole(UserRole.ADMIN),
                userRepo.countByRole(UserRole.MENTOR)
        );
    }
}