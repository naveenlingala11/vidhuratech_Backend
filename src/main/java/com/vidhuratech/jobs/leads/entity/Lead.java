package com.vidhuratech.jobs.leads.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Data
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String email;
    private String course;
    private String experience;
    private String batch;
    private String city;
    private String message;

    private String status = "New";
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private Boolean deleted = false;

    private LocalDateTime deletedAt;
    private LocalDate followUpDate;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}