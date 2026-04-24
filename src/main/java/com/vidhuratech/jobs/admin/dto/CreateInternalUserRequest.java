package com.vidhuratech.jobs.admin.dto;

import com.vidhuratech.jobs.user.enums.UserRole;
import lombok.Data;

@Data
public class CreateInternalUserRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
    private UserRole role;
}