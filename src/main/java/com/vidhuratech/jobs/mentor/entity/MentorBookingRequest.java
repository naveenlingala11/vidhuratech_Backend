package com.vidhuratech.jobs.mentor.entity;

import com.vidhuratech.jobs.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_booking_requests")
@Getter
@Setter
public class MentorBookingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "student_phone")
    private String studentPhone;

    @Column(name = "student_email")
    private String studentEmail;

    private String topic;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "preferred_plan")
    private String preferredPlan = "HOURLY";

    private String status = "PENDING"; // PENDING, ACCEPTED, REJECTED

    @Column(name = "mentor_note", columnDefinition = "TEXT")
    private String mentorNote;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
