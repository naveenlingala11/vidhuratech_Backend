package com.vidhuratech.jobs.publicpractice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "public_assessment_attempts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicAssessmentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accessGrantId;
    private Long leadId;
    private Long assessmentId;

    private Integer score;
    private Integer totalMarks;
    private Integer percentage;
    private Integer correctAnswers;
    private Integer totalQuestions;

    private String status;
    private LocalDateTime submittedAt;
}