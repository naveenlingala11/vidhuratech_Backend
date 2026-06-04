package com.vidhuratech.jobs.publicpractice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "public_challenge_discussion_reports",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_public_discussion_report_user",
                        columnNames = {"discussionId", "reporterKey"}
                )
        }
)
@Data
public class PublicChallengeDiscussionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long discussionId;

    @Column(length = 180)
    private String reporterKey;

    @Column(length = 500)
    private String reason;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}