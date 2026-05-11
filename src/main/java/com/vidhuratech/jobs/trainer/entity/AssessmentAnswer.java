package com.vidhuratech.jobs.trainer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private AssessmentAttempt attempt;

    @ManyToOne
    private AssessmentQuestion question;

    private String selectedAnswer;

    private Boolean correct;
}