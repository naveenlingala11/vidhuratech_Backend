package com.vidhuratech.jobs.invoice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    private String id;

    private String leadPhone;

    private String name;
    private String email;
    private String mobile;
    private String studentAddress;

    private String course;
    private String batch;
    private String trainer;

    private Double amount;
    private Double discount;
    private Double scholarship;

    private Double paidAmount;
    private Double remainingAmount;

    private Boolean installmentEnabled;

    private String couponCode;

    private String paymentStatus;
    private String paymentMethod;

    @Column(length = 2000)
    private String notes;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    private String utrNumber;

    private String paymentScreenshotUrl;

    private Boolean paymentVerified = false;

    private LocalDateTime verifiedAt;

    private String verifiedBy;
}