package com.vidhuratech.jobs.plans.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_pricing_controls")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanPricingControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String planCode;
    private String planName;

    private Double price;
    private Double compareAtPrice;

    private Integer durationDays;
    private Integer companyLimit;

    private Boolean highlighted;
    private Boolean active;

    private LocalDateTime updatedAt;
}