package com.vidhuratech.jobs.mentor.entity;

import com.vidhuratech.jobs.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_profiles")
@Getter
@Setter
public class MentorProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "current_company")
    private String currentCompany;

    @Column(name = "job_role")
    private String currentRole;

    @Column(name = "years_of_experience")
    private Double yearsOfExperience = 0.0;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(length = 1000)
    private String skills;

    private String languages;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    private Double rating = 5.0;

    @Column(name = "reviews_count")
    private Integer reviewsCount = 0;

    @Column(name = "price_per_hour", precision = 10, scale = 2)
    private BigDecimal pricePerHour = BigDecimal.ZERO;

    @Column(name = "price_per_week", precision = 10, scale = 2)
    private BigDecimal pricePerWeek = BigDecimal.ZERO;

    @Column(name = "price_per_month", precision = 10, scale = 2)
    private BigDecimal pricePerMonth = BigDecimal.ZERO;

    @Column(name = "availability_days", length = 500)
    private String availabilityDays = "monday,tuesday,wednesday,thursday,friday";

    @Column(name = "availability_slots", length = 500)
    private String availabilitySlots = "evening";

    @Column(name = "allow_daily_sessions")
    private Boolean allowDailySessions = false;

    private Boolean featured = false;

    private Boolean active = true;

    @Column(name = "identity_verified")
    private Boolean identityVerified = false;

    @Column(name = "company_verified")
    private Boolean companyVerified = false;

    @Column(name = "linkedin_verified")
    private Boolean linkedinVerified = false;

    @Column(name = "cert_verified")
    private Boolean certVerified = false;

    @Column(name = "terms_verified")
    private Boolean termsVerified = false;

    @Column(name = "verification_document_url", length = 1000)
    private String verificationDocumentUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
