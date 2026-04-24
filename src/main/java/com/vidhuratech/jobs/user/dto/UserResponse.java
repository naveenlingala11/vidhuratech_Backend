package com.vidhuratech.jobs.user.dto;

import com.vidhuratech.jobs.user.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private Boolean active;
}
