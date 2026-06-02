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
            case "STARTER" -> plan("STARTER", "Starter", 499.0, 30, 5, false, true, true, true, true, false, false, true, false);
            case "PRO" -> plan("PRO", "Pro", 1499.0, 30, 15, true, true, true, true, true, true, true, true, true);
            case "ELITE" -> plan("ELITE", "Elite", 4999.0, 180, 999, true, true, true, true, true, true, true, true, true);
            default -> throw new RuntimeException("Invalid plan selected");
        };

        return applyPricingControl(planCode, plan);
    }

    public List<Map<String, Object>> listPlans() {
        return List.of(getPlan("STARTER"), getPlan("PRO"), getPlan("ELITE"));
    }

    private Map<String, Object> plan(
            String code,
            String name,
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
        map.put("amount", amount);
        map.put("price", amount);
        map.put("compareAtPrice", null);
        map.put("validityDays", validityDays);
        map.put("durationDays", validityDays);
        map.put("companyLimit", companyLimit);
        map.put("highlighted", "PRO".equalsIgnoreCase(code));
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
        }

        plan.put("highlighted", Boolean.TRUE.equals(pricing.getHighlighted()));
        plan.put("active", !Boolean.FALSE.equals(pricing.getActive()));
        plan.put("updatedAt", pricing.getUpdatedAt());

        return plan;
    }
}