package com.vidhuratech.jobs.user.entity;

import com.vidhuratech.jobs.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String phone;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.STUDENT;

    private Boolean active = true;

    private Boolean deleted = false;

    private Long createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    private Boolean firstLogin = true;

    @Column(nullable = false)
    private Boolean notificationsEnabled = true;

    @Column(columnDefinition = "TEXT")
    private String profileImageUrl;

    @Column(name = "reputation_points")
    private Integer reputationPoints = 0;

    @Column(name = "reputation_level")
    private String reputationLevel = "BEGINNER";

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(name = "social_links", columnDefinition = "TEXT")
    private String socialLinks;

    @Column(name = "member_since")
    private LocalDateTime memberSince;
}