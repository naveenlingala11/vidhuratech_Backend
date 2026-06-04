package com.vidhuratech.jobs.publicpractice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "public_challenge_discussion_blocks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_public_discussion_block_user",
                        columnNames = {"blockerKey", "blockedAuthorKey"}
                )
        }
)
@Data
public class PublicChallengeDiscussionBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 180)
    private String blockerKey;

    @Column(length = 180)
    private String blockedAuthorKey;

    @Column(length = 180)
    private String blockedAuthorName;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}