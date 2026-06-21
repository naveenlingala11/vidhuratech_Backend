package com.vidhuratech.jobs.mentor.dto;

import com.vidhuratech.jobs.mentor.entity.MentorProfile;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class MentorProfileResponse {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String profileImageUrl;
    private String currentCompany;
    private String currentRole;
    private Double yearsOfExperience;
    private String biography;
    private String skills;
    private String languages;
    private String linkedinUrl;
    private String githubUrl;
    private Double rating;
    private Integer reviewsCount;
    private BigDecimal pricePerHour;
    private BigDecimal pricePerWeek;
    private BigDecimal pricePerMonth;
    private String availabilityDays;
    private String availabilitySlots;
    private Boolean allowDailySessions;
    private Boolean featured;
    private Boolean active;
    private Boolean identityVerified;
    private Boolean companyVerified;
    private Boolean linkedinVerified;
    private Boolean certVerified;
    private Boolean termsVerified;
    private String verificationDocumentUrl;

    public MentorProfileResponse(MentorProfile profile) {
        if (profile != null) {
            this.userId = profile.getUserId();
            this.currentCompany = profile.getCurrentCompany();
            this.currentRole = profile.getCurrentRole();
            this.yearsOfExperience = profile.getYearsOfExperience();
            this.biography = profile.getBiography();
            this.skills = profile.getSkills();
            this.languages = profile.getLanguages();
            this.linkedinUrl = profile.getLinkedinUrl();
            this.githubUrl = profile.getGithubUrl();
            this.rating = profile.getRating();
            this.reviewsCount = profile.getReviewsCount();
            this.pricePerHour = profile.getPricePerHour();
            this.pricePerWeek = profile.getPricePerWeek();
            this.pricePerMonth = profile.getPricePerMonth();
            this.availabilityDays = profile.getAvailabilityDays();
            this.availabilitySlots = profile.getAvailabilitySlots();
            this.allowDailySessions = profile.getAllowDailySessions();
            this.featured = profile.getFeatured();
            this.active = profile.getActive();
            this.identityVerified = profile.getIdentityVerified() != null ? profile.getIdentityVerified() : false;
            this.companyVerified = profile.getCompanyVerified() != null ? profile.getCompanyVerified() : false;
            this.linkedinVerified = profile.getLinkedinVerified() != null ? profile.getLinkedinVerified() : false;
            this.certVerified = profile.getCertVerified() != null ? profile.getCertVerified() : false;
            this.termsVerified = profile.getTermsVerified() != null ? profile.getTermsVerified() : false;
            this.verificationDocumentUrl = profile.getVerificationDocumentUrl();

            if (profile.getUser() != null) {
                this.name = profile.getUser().getName();
                this.profileImageUrl = profile.getUser().getProfileImageUrl();

                // Secure contact details: Only expose email/phone to the mentor themselves or an admin
                org.springframework.security.core.Authentication auth = 
                        org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()) {
                    String loggedInEmail = auth.getName();
                    boolean isAdmin = auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    String mentorEmailVal = profile.getUser().getEmail();
                    
                    if (isAdmin || (mentorEmailVal != null && mentorEmailVal.equalsIgnoreCase(loggedInEmail))) {
                        this.email = mentorEmailVal;
                        this.phone = profile.getUser().getPhone();
                    } else {
                        this.email = null;
                        this.phone = null;
                    }
                } else {
                    this.email = null;
                    this.phone = null;
                }
            }
        }
    }
}
