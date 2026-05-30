package com.vidhuratech.jobs.prep.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vidhuratech.jobs.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

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

    private Long batchId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private User trainer;

    private Boolean active = true;
    private Boolean publicVisible = false;
    private String publicAccessLevel = "LEAD_REQUIRED";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private Long publishedByUserId;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (active == null) active = true;
        if (publicVisible == null) publicVisible = false;
        if (publicAccessLevel == null) publicAccessLevel = "LEAD_REQUIRED";
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}