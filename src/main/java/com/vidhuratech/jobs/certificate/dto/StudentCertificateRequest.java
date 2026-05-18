package com.vidhuratech.jobs.certificate.dto;

import lombok.Data;

@Data
public class StudentCertificateRequest {
    private String name;
    private String email;
    private String mobile;
    private String course;
    private String batchId;
}