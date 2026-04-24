package com.vidhuratech.jobs.user.dto;

import com.vidhuratech.jobs.user.enums.UserRole;
import lombok.Data;

@Data
public class CreateEmployeeDTO {
    private String name;
    private String email;
    private String phone;
    private UserRole role;
}