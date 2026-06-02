package com.vidhuratech.jobs.plans.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_access_grants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanAccessGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String invoiceId;
    private String planCode;
    private String planName;

    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;

    private Double amount;
    private String status;

    private Boolean accessCourses;
    private Boolean accessMockTests;
    private Boolean accessInterviews;
    private Boolean accessNotes;
    private Boolean accessMaterials;
    private Boolean accessVideos;
    private Boolean accessLiveClasses;
    private Boolean accessPracticeCompanies;
    private Boolean accessPremiumChallenges;

    private Integer companyLimit;

    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}