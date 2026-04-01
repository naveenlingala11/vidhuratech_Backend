package com.vidhuratech.jobs.prep.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "interview_questions")
@Data
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String company;
    private String role;

    private String type;
    private String topic;
    private String difficulty;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;
}
