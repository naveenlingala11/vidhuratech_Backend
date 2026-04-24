package com.vidhuratech.jobs.user.helper;

import com.vidhuratech.jobs.user.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class RoleHierarchyUtil {

    public boolean canCreate(UserRole creator, UserRole target) {
        return switch (creator) {
            case SUPER_ADMIN -> true;

            case ADMIN -> target == UserRole.TRAINER
                    || target == UserRole.HR
                    || target == UserRole.MANAGER
                    || target == UserRole.MENTOR;

            case MANAGER -> target == UserRole.MENTOR;

            case HR -> target == UserRole.STUDENT;

            default -> false;
        };
    }
}