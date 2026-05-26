package com.vidhuratech.jobs.user.dto;

import com.vidhuratech.jobs.user.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDTO {
    private String name;
    private String email;
    private String phone;
    private UserRole role;
}