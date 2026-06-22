package com.vidhuratech.jobs.mentor.entity;

import com.vidhuratech.jobs.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_sessions")
@Getter
@Setter
public class MentorSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "session_date", nullable = false)
    private String sessionDate;

    @Column(name = "session_time", nullable = false)
    private String sessionTime;

    @Column(name = "session_type")
    private String sessionType = "Mock Interview";

    @Column(name = "meeting_link", columnDefinition = "TEXT", nullable = false)
    private String meetingLink;

    private String status = "SCHEDULED";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
