package com.vidhuratech.jobs.admission.dto;

import lombok.Data;

@Data
public class ManualAdmissionRequest {

    private String name;

    private String email;

    private String phone;

    private Long batchId;

    private Double amount;

    // CASH / UPI / BANK_TRANSFER
    private String paymentMethod;

    // PAID / PARTIAL / PENDING
    private String paymentStatus;
}