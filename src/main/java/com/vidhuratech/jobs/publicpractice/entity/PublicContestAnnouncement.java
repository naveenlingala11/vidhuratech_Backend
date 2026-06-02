package com.vidhuratech.jobs.publicpractice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "public_contest_announcements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicContestAnnouncement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime weekStart;
    private LocalDateTime weekEnd;
    private Long challengeId;

    @Column(columnDefinition = "TEXT")
    private String winnersJson;

    private Boolean published;
    private LocalDateTime createdAt;
}