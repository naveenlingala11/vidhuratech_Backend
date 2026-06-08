package com.vidhuratech.jobs.publicpractice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "public_challenge_discussions")
@Data
public class PublicChallengeDiscussion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long challengeId;

    private Long parentId;

    @Column(length = 140)
    private String authorName;

    @Column(length = 180)
    private String authorEmail;

    @Column(length = 1000)
    private String authorProfileImageUrl;

    @Column(length = 140)
    private String authorKey;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private Integer likeCount = 0;

    private Integer reportCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (likeCount == null) {
            likeCount = 0;
        }

        if (reportCount == null) {
            reportCount = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}