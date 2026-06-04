package com.vidhuratech.jobs.plans.service;

import com.vidhuratech.jobs.plans.entity.PlanPricingControl;
import com.vidhuratech.jobs.plans.repository.PlanPricingControlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PlanCatalogService {

    private final PlanPricingControlRepository pricingRepository;

    public Map<String, Object> getPlan(String code) {
        String planCode = String.valueOf(code == null ? "" : code).trim().toUpperCase();

        Map<String, Object> plan = switch (planCode) {
            case "STARTER", "BASIC" -> basicPlan();
            case "PRO" -> proPlan();
            case "ELITE" -> elitePlan();
            default -> throw new RuntimeException("Invalid plan selected");
        };

        return applyPricingControl(String.valueOf(plan.get("code")), plan);
    }

    public List<Map<String, Object>> listPlans() {
        return List.of(getPlan("STARTER"), getPlan("PRO"), getPlan("ELITE"));
    }

    private Map<String, Object> basicPlan() {
        Map<String, Object> plan = plan(
                "STARTER",
                "Basic",
                "Basic Practice Access",
                "For learners who want affordable practice access.",
                "Start with mock tests, company-wise practice, discussions, and best-answer viewing.",
                49.0,
                30,
                5,
                false,
                true,
                true,
                true,
                true,
                false,
                false,
                true,
                false
        );

        plan.put("tier", "BASIC");
        plan.put("badge", "Best Entry Plan");
        plan.put("recommended", false);
        plan.put("highlighted", false);
        plan.put("priority", 1);
        plan.put("bestFor", List.of(
                "Students starting placement preparation",
                "Learners who need mock tests and practice access",
                "Users who want to unlock Best Answers at minimum cost"
        ));
        plan.put("features", List.of(
                "Access to company-wise mock tests",
                "Access to basic coding challenges",
                "View Best Answers for coding challenges",
                "View 80%+ submitted answer code",
                "Interview preparation question bank",
                "Premium notes and revision sheets",
                "Practice for 5 company bundles",
                "Leaderboard visibility",
                "Discussion participation"
        ));
        plan.put("limitations", List.of(
                "No premium video lessons",
                "No live classes",
                "No full course access",
                "Limited company practice bundles"
        ));
        plan.put("accessSummary", Map.of(
                "bestAnswers", true,
                "mockTests", true,
                "codingChallenges", "Basic challenges",
                "premiumChallenges", false,
                "courses", false,
                "videos", false,
                "liveClasses", false,
                "companyPracticeLimit", 5
        ));

        return plan;
    }

    private Map<String, Object> proPlan() {
        Map<String, Object> plan = plan(
                "PRO",
                "Pro",
                "Premium Practice + Learning",
                "For serious learners preparing for placements and coding rounds.",
                "Unlock premium challenges, materials, videos, live support, and stronger practice coverage.",
                149.0,
                30,
                15,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true
        );

        plan.put("tier", "PRO");
        plan.put("badge", "Most Popular");
        plan.put("recommended", true);
        plan.put("highlighted", true);
        plan.put("priority", 2);
        plan.put("bestFor", List.of(
                "Students preparing seriously for placements",
                "Learners who need premium coding challenges",
                "Users who want videos, materials, live sessions, and practice"
        ));
        plan.put("features", List.of(
                "Everything included in Basic",
                "Access to premium coding challenges",
                "View Best Answers and submitted answer code",
                "Mock tests and assessment practice",
                "Interview preparation access",
                "Recorded video lessons",
                "Downloadable preparation materials",
                "Live classes and doubt sessions",
                "Practice for 15 company bundles",
                "Premium notes and revision sheets",
                "Leaderboard and contest access"
        ));
        plan.put("limitations", List.of(
                "Limited to 15 company practice bundles",
                "Does not include long-duration Elite bundle validity"
        ));
        plan.put("accessSummary", Map.of(
                "bestAnswers", true,
                "mockTests", true,
                "codingChallenges", "Premium challenges",
                "premiumChallenges", true,
                "courses", true,
                "videos", true,
                "liveClasses", true,
                "companyPracticeLimit", 15
        ));

        return plan;
    }

    private Map<String, Object> elitePlan() {
        Map<String, Object> plan = plan(
                "ELITE",
                "Elite",
                "Complete Career Bundle",
                "For learners who want complete access and long-duration preparation.",
                "Complete bundle with premium practice, courses, live classes, videos, and placement preparation.",
                499.0,
                180,
                999,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true
        );

        plan.put("tier", "ELITE");
        plan.put("badge", "Complete Bundle");
        plan.put("recommended", false);
        plan.put("highlighted", false);
        plan.put("priority", 3);
        plan.put("bestFor", List.of(
                "Career-focused learners",
                "Students who want complete preparation access",
                "Users who need long-duration access with maximum practice coverage"
        ));
        plan.put("features", List.of(
                "Everything included in Pro",
                "Complete 180-day access",
                "Unlimited company-wise practice bundles",
                "All premium coding challenges",
                "View Best Answers and submitted answer code",
                "All premium courses access",
                "Live classes and doubt support",
                "Recorded video lessons",
                "Downloadable materials and notes",
                "Interview preparation support",
                "Advanced coding contests",
                "Mock interview preparation resources",
                "Placement preparation bundle"
        ));
        plan.put("limitations", List.of(
                "No major access limitations during validity"
        ));
        plan.put("accessSummary", Map.of(
                "bestAnswers", true,
                "mockTests", true,
                "codingChallenges", "All premium challenges",
                "premiumChallenges", true,
                "courses", true,
                "videos", true,
                "liveClasses", true,
                "companyPracticeLimit", 999
        ));

        return plan;
    }

    private Map<String, Object> plan(
            String code,
            String name,
            String tagline,
            String audience,
            String description,
            Double amount,
            Integer validityDays,
            Integer companyLimit,
            Boolean courses,
            Boolean mockTests,
            Boolean interviews,
            Boolean notes,
            Boolean materials,
            Boolean videos,
            Boolean liveClasses,
            Boolean practiceCompanies,
            Boolean premiumChallenges
    ) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("code", code);
        map.put("name", name);
        map.put("planName", name);
        map.put("tagline", tagline);
        map.put("audience", audience);
        map.put("description", description);
        map.put("amount", amount);
        map.put("price", amount);
        map.put("currency", "INR");
        map.put("compareAtPrice", null);
        map.put("validityDays", validityDays);
        map.put("durationDays", validityDays);
        map.put("companyLimit", companyLimit);
        map.put("active", true);

        map.put("accessCourses", courses);
        map.put("accessMockTests", mockTests);
        map.put("accessInterviews", interviews);
        map.put("accessNotes", notes);
        map.put("accessMaterials", materials);
        map.put("accessVideos", videos);
        map.put("accessLiveClasses", liveClasses);
        map.put("accessPracticeCompanies", practiceCompanies);
        map.put("accessPremiumChallenges", premiumChallenges);
        map.put("accessBestAnswers", true);

        map.put("legalNote", "Plan access is valid only during the active subscription period.");
        map.put("billingNote", "Amount shown is the current platform access price.");
        map.put("refundNote", "Refunds, if applicable, follow the platform refund policy.");

        return map;
    }

    private Map<String, Object> applyPricingControl(String planCode, Map<String, Object> plan) {
        Optional<PlanPricingControl> control = pricingRepository.findByPlanCodeIgnoreCase(planCode);

        if (control.isEmpty()) {
            return plan;
        }

        PlanPricingControl pricing = control.get();

        if (pricing.getPlanName() != null && !pricing.getPlanName().isBlank()) {
            plan.put("name", pricing.getPlanName());
            plan.put("planName", pricing.getPlanName());
        }

        if (pricing.getPrice() != null) {
            plan.put("amount", pricing.getPrice());
            plan.put("price", pricing.getPrice());
        }

        plan.put("compareAtPrice", pricing.getCompareAtPrice());

        if (pricing.getDurationDays() != null) {
            plan.put("validityDays", pricing.getDurationDays());
            plan.put("durationDays", pricing.getDurationDays());
        }

        if (pricing.getCompanyLimit() != null) {
            plan.put("companyLimit", pricing.getCompanyLimit());

            Object accessSummaryRaw = plan.get("accessSummary");
            if (accessSummaryRaw instanceof Map<?, ?> accessSummary) {
                Map<String, Object> updatedSummary = new LinkedHashMap<>();

                for (Map.Entry<?, ?> entry : accessSummary.entrySet()) {
                    updatedSummary.put(String.valueOf(entry.getKey()), entry.getValue());
                }

                updatedSummary.put("companyPracticeLimit", pricing.getCompanyLimit());
                plan.put("accessSummary", updatedSummary);
            }
        }

        plan.put("highlighted", Boolean.TRUE.equals(pricing.getHighlighted()));
        plan.put("recommended", Boolean.TRUE.equals(pricing.getHighlighted()));
        plan.put("active", !Boolean.FALSE.equals(pricing.getActive()));
        plan.put("updatedAt", pricing.getUpdatedAt());

        return plan;
    }
}