package com.vidhuratech.jobs.publicpractice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "public_challenge_discussion_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_public_discussion_like_user",
                        columnNames = {"discussionId", "likerKey"}
                )
        }
)
@Data
public class PublicChallengeDiscussionLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long discussionId;

    @Column(length = 140)
    private String likerKey;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}