package com.vidhuratech.jobs.trainer.entity;

import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "mock_interview_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockInterviewRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private User trainer;

    private String topic;

    private LocalDate preferredDate;

    private LocalTime preferredTime;

    @Column(length = 2000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MockInterviewStatus status = MockInterviewStatus.REQUESTED;

    private String meetingLink;

    @Column(length = 2000)
    private String trainerRemarks;

    @Column(columnDefinition = "TEXT")
    private String sessionSummary;

    @Column(columnDefinition = "TEXT")
    private String sessionChat;

    private LocalDateTime expirationDate;

    private Integer maxDurationMinutes;

    private Integer actualDurationMinutes;

    @Builder.Default
    private Boolean isEnded = false;

    private Integer participantCount;

    @Column(columnDefinition = "TEXT")
    private String meetingLogs;

    private String candidateName;

    private String candidateEmail;

    private String hostName;

    private String hostEmail;

    private String hostRole;

    @Builder.Default
    private Boolean isPublic = false;

    @Builder.Default
    private Integer joinCount = 0;

    @Builder.Default
    private String recurringType = "ONCE";

    private String recurringDays;

    @Column(columnDefinition = "TEXT")
    private String invitedEmails;

    private LocalTime preferredEndTime;

    @Builder.Default
    private String timezone = "Asia/Kolkata";

    private Integer feedbackRating;

    @Column(columnDefinition = "TEXT")
    private String feedbackText;

    private String feedbackUser;

    private String feedbackEmail;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}

