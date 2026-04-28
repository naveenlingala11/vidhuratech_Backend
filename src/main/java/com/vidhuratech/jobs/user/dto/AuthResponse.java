package com.vidhuratech.jobs.user.dto;

import com.vidhuratech.jobs.user.enums.UserRole;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;
    private UserRole role;
    private String name;
    private String email;
    private String phone;
    private Boolean active;
    private boolean firstLogin;
}
