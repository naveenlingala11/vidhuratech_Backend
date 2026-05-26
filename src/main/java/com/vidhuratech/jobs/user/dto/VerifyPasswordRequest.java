package com.vidhuratech.jobs.user.dto;

import lombok.Data;

@Data
public class VerifyPasswordRequest {
    private String currentPassword;
}