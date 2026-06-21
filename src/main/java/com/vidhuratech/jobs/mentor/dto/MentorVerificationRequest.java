package com.vidhuratech.jobs.mentor.dto;

import lombok.Data;

@Data
public class MentorVerificationRequest {
    private Boolean identityVerified;
    private Boolean companyVerified;
    private Boolean linkedinVerified;
    private Boolean certVerified;
    private Boolean termsVerified;
    private String verificationDocumentUrl;
}
