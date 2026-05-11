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

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(optional = false)
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

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}

