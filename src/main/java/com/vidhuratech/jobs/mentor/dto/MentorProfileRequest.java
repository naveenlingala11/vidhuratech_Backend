package com.vidhuratech.jobs.mentor.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MentorProfileRequest {
    private String currentCompany;
    private String currentRole;
    private Double yearsOfExperience;
    private String biography;
    private String skills;
    private String languages;
    private String linkedinUrl;
    private String githubUrl;
    private BigDecimal pricePerHour;
    private BigDecimal pricePerWeek;
    private BigDecimal pricePerMonth;
    private String availabilityDays;
    private String availabilitySlots;
    private Boolean allowDailySessions;
}
