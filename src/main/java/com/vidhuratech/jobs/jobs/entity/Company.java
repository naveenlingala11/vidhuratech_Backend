package com.vidhuratech.jobs.jobs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String logoUrl;

    private String website;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Company(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}