package com.vidhuratech.jobs.publicpractice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "public_practice_access_grants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicPracticeAccessGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long leadId;

    private String practiceType;

    private Long practiceId;

    private String accessLevel;

    @Column(unique = true, length = 500)
    private String accessToken;

    private Integer maxAttempts;

    private Integer attemptsUsed;

    private LocalDateTime expiresAt;

    private Boolean active;

    private LocalDateTime createdAt;
}