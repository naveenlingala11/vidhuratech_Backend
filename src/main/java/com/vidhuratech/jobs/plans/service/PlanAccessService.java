package com.vidhuratech.jobs.plans.service;

import com.vidhuratech.jobs.plans.entity.PlanAccessGrant;
import com.vidhuratech.jobs.plans.repository.PlanAccessGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanAccessService {

    private final PlanAccessGrantRepository repo;

    public boolean hasPremiumChallengeAccess(Long userId, String email) {
        return activeGrants(userId, email)
                .stream()
                .anyMatch(grant -> Boolean.TRUE.equals(grant.getAccessPremiumChallenges()));
    }

    public boolean hasFeatureAccess(Long userId, String email, String feature) {
        String key = String.valueOf(feature == null ? "" : feature).trim().toUpperCase();

        return activeGrants(userId, email).stream().anyMatch(grant -> switch (key) {
            case "COURSES" -> Boolean.TRUE.equals(grant.getAccessCourses());
            case "MOCK_TESTS" -> Boolean.TRUE.equals(grant.getAccessMockTests());
            case "INTERVIEWS" -> Boolean.TRUE.equals(grant.getAccessInterviews());
            case "NOTES" -> Boolean.TRUE.equals(grant.getAccessNotes());
            case "MATERIALS" -> Boolean.TRUE.equals(grant.getAccessMaterials());
            case "VIDEOS" -> Boolean.TRUE.equals(grant.getAccessVideos());
            case "LIVE_CLASSES" -> Boolean.TRUE.equals(grant.getAccessLiveClasses());
            case "PRACTICE_COMPANIES" -> Boolean.TRUE.equals(grant.getAccessPracticeCompanies());
            case "PREMIUM_CHALLENGES" -> Boolean.TRUE.equals(grant.getAccessPremiumChallenges());
            default -> false;
        });
    }

    public List<PlanAccessGrant> activeGrants(Long userId, String email) {
        LocalDateTime now = LocalDateTime.now();

        if (userId != null) {
            List<PlanAccessGrant> byUser = repo.findByUserIdAndStatusAndExpiresAtAfter(
                    userId,
                    "ACTIVE",
                    now
            );

            if (!byUser.isEmpty()) {
                return byUser;
            }
        }

        if (email != null && !email.isBlank()) {
            return repo.findByBuyerEmailIgnoreCaseAndStatusAndExpiresAtAfter(
                    email,
                    "ACTIVE",
                    now
            );
        }

        return List.of();
    }
}