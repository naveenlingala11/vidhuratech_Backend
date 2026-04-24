package com.vidhuratech.jobs.lms.batch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "batch_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private Batch batch;

    private String title;

    @Column(length = 5000)
    private String description;

    private String videoUrl;

    private Integer durationMinutes;

    private LocalDate sessionDate;

    private Boolean published = false;
}