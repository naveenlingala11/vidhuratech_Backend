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

    public boolean hasAnyActivePlan(Long userId, String email) {
        return !activeGrants(userId, email).isEmpty();
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

    public boolean hasTierAtLeast(Long userId, String email, String requiredTier) {
        int requiredRank = tierRank(requiredTier);

        return activeGrants(userId, email)
                .stream()
                .anyMatch(grant -> tierRank(resolveTier(grant.getPlanCode(), grant.getPlanName())) >= requiredRank);
    }

    private String resolveTier(String planCode, String planName) {
        String text = ((planCode == null ? "" : planCode) + " " + (planName == null ? "" : planName))
                .trim()
                .toUpperCase();

        if (text.contains("ELITE")) return "ELITE";
        if (text.contains("PRO")) return "PRO";
        if (text.contains("BASIC") || text.contains("STARTER")) return "BASIC";

        return "";
    }

    private int tierRank(String tier) {
        if (tier == null) return 0;

        return switch (tier.trim().toUpperCase()) {
            case "BASIC", "STARTER" -> 1;
            case "PRO" -> 2;
            case "ELITE" -> 3;
            default -> 0;
        };
    }
}