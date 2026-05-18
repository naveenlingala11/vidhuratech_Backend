package com.vidhuratech.jobs.trainer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assessment_answers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "attempt_id",
            nullable = false
    )
    private AssessmentAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "question_id",
            nullable = false
    )
    private AssessmentQuestion question;

    @Column(nullable = false)
    private String selectedAnswer;

    private Boolean correct;
}