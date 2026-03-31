package com.vidhuratech.jobs.leads.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_access")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String phone;

    private boolean access;

    private LocalDateTime createdAt = LocalDateTime.now();
}
