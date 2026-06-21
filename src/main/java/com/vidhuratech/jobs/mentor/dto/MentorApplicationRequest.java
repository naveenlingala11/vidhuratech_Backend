package com.vidhuratech.jobs.mentor.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MentorApplicationRequest {
    // User Account Details
    private String name;
    private String email;
    private String phone;
    private String password;

    // Mentor Profile Details
    private String currentCompany;
    private String currentRole;
    private Double yearsOfExperience;
    private String biography;
    private String skills;
    private String languages;
    private String linkedinUrl;
    private String githubUrl;
    private BigDecimal pricePerHour;
    private String verificationDocumentUrl;
    private String profileImageUrl;
}
