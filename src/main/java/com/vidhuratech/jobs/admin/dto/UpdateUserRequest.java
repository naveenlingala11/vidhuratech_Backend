package com.vidhuratech.jobs.admin.dto;

import com.vidhuratech.jobs.user.enums.UserRole;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String phone;
    private UserRole role;
    private Boolean active;
}