package com.vidhuratech.jobs.plans.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.plans.dto.PlanCheckoutRequest;
import com.vidhuratech.jobs.plans.dto.PlanPaymentConfirmRequest;
import com.vidhuratech.jobs.plans.entity.PlanAccessGrant;
import com.vidhuratech.jobs.plans.service.PlanAccessService;
import com.vidhuratech.jobs.plans.service.PlanCatalogService;
import com.vidhuratech.jobs.plans.service.PlanCheckoutService;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/plans")
@RequiredArgsConstructor
@CrossOrigin
public class PublicPlanController {

    private final PlanCatalogService planCatalogService;
    private final PlanCheckoutService planCheckoutService;
    private final PlanAccessService planAccessService;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<?> plans() {
        return ApiResponse.builder()
                .success(true)
                .data(planCatalogService.listPlans())
                .build();
    }

    @PostMapping("/checkout")
    public ApiResponse<?> checkout(@RequestBody PlanCheckoutRequest request) {
        return ApiResponse.builder()
                .success(true)
                .message("Plan checkout created")
                .data(planCheckoutService.initiate(request))
                .build();
    }

    @PostMapping("/confirm")
    public ApiResponse<?> confirm(@RequestBody PlanPaymentConfirmRequest request) {
        return ApiResponse.builder()
                .success(true)
                .message("Payment confirmed and plan activated")
                .data(planCheckoutService.confirm(request))
                .build();
    }

    @GetMapping("/my-access")
    public ApiResponse<?> myAccess() {
        Long userId = securityUtils.getCurrentUserId();

        if (userId == null) {
            return ApiResponse.builder()
                    .success(true)
                    .data(Map.of(
                            "loggedIn", false,
                            "active", false
                    ))
                    .build();
        }

        String email = userRepository.findById(userId)
                .map(user -> user.getEmail())
                .orElse("");

        List<PlanAccessGrant> grants = planAccessService.activeGrants(userId, email);

        boolean courses = grants.stream().anyMatch(g -> Boolean.TRUE.equals(g.getAccessCourses()));
        boolean mockTests = grants.stream().anyMatch(g -> Boolean.TRUE.equals(g.getAccessMockTests()));
        boolean interviews = grants.stream().anyMatch(g -> Boolean.TRUE.equals(g.getAccessInterviews()));
        boolean notes = grants.stream().anyMatch(g -> Boolean.TRUE.equals(g.getAccessNotes()));
        boolean materials = grants.stream().anyMatch(g -> Boolean.TRUE.equals(g.getAccessMaterials()));
        boolean videos = grants.stream().anyMatch(g -> Boolean.TRUE.equals(g.getAccessVideos()));
        boolean liveClasses = grants.stream().anyMatch(g -> Boolean.TRUE.equals(g.getAccessLiveClasses()));
        boolean practiceCompanies = grants.stream().anyMatch(g -> Boolean.TRUE.equals(g.getAccessPracticeCompanies()));
        boolean premiumChallenges = grants.stream().anyMatch(g -> Boolean.TRUE.equals(g.getAccessPremiumChallenges()));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("loggedIn", true);
        data.put("active", !grants.isEmpty());
        data.put("accessCourses", courses);
        data.put("accessMockTests", mockTests);
        data.put("accessInterviews", interviews);
        data.put("accessNotes", notes);
        data.put("accessMaterials", materials);
        data.put("accessVideos", videos);
        data.put("accessLiveClasses", liveClasses);
        data.put("accessPracticeCompanies", practiceCompanies);
        data.put("accessPremiumChallenges", premiumChallenges);

        data.put(
                "plans",
                grants.stream().map(grant -> Map.of(
                        "planCode", grant.getPlanCode(),
                        "planName", grant.getPlanName(),
                        "expiresAt", grant.getExpiresAt()
                )).toList()
        );

        return ApiResponse.builder()
                .success(true)
                .data(data)
                .build();
    }
}