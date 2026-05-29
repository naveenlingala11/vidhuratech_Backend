package com.vidhuratech.jobs.common.notification.entity;

import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id")
    private User recipientUser;

    @Enumerated(EnumType.STRING)
    private UserRole recipientRole;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String activityType;

    private String link;

    @Builder.Default
    @Column(name = "read_flag", nullable = false)
    private Boolean read = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailSent = false;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (read == null) {
            read = false;
        }

        if (emailSent == null) {
            emailSent = false;
        }
    }
}