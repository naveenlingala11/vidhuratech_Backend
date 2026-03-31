package com.vidhuratech.jobs.jobs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Job job;

    private long views;
    private long clicks;
    private long applyClicks;

    private LocalDateTime lastViewed;
}