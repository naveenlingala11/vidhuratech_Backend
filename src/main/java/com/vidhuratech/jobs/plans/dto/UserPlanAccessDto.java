package com.vidhuratech.jobs.plans.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPlanAccessDto {

    private boolean active;
    private boolean enrolledStudent;
    private String tier;
}