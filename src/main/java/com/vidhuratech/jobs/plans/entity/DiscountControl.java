package com.vidhuratech.jobs.plans.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "discount_controls")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String title;

    private String discountType;
    private Double discountValue;

    private String planCode;

    private Integer maxUses;
    private Integer usedCount;

    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}