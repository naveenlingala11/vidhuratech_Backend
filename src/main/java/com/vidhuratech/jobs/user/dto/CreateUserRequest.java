package com.vidhuratech.jobs.user.dto;

import com.vidhuratech.jobs.user.enums.UserRole;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
    private UserRole role;
}