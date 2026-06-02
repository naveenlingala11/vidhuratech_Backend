package com.vidhuratech.jobs.plans.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_access_controls")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAccessControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String controlKey;
    private String label;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Boolean enabled;

    private LocalDateTime updatedAt;
}