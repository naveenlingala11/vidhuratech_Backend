package com.vidhuratech.jobs.publicpractice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "public_challenge_attempts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicChallengeAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accessGrantId;
    private Long leadId;
    private Long challengeId;

    private String language;

    @Column(columnDefinition = "TEXT")
    private String sourceCode;

    private Integer score;
    private Integer totalMarks;
    private Integer percentage;

    private String status;
    private LocalDateTime submittedAt;
}