package com.vidhuratech.jobs.plans.dto;

import lombok.Data;

@Data
public class PlanCheckoutRequest {
    private String planCode;
    private String name;
    private String email;
    private String phone;
    private String city;
}