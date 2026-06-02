package com.vidhuratech.jobs.plans.dto;

import lombok.Data;

@Data
public class PlanPaymentConfirmRequest {
    private String invoiceId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}