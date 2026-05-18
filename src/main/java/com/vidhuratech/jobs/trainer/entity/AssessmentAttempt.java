package com.vidhuratech.jobs.trainer.entity;

import com.vidhuratech.jobs.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_attempts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assessment_id")
    private Assessment assessment;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    private Integer score;

    private Integer correctAnswers;

    private Integer totalQuestions;

    private LocalDateTime submittedAt;
}