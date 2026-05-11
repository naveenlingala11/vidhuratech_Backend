package com.vidhuratech.jobs.trainer.entity;

import com.vidhuratech.jobs.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "training_submissions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"work_item_id", "student_id"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "work_item_id")
    private TrainingWorkItem workItem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @Column(length = 5000)
    private String answerText;

    private Integer marks;

    @Column(length = 2000)
    private String feedback;

    @Enumerated(EnumType.STRING)
    private TrainingSubmissionStatus status = TrainingSubmissionStatus.SUBMITTED;

    private LocalDateTime submittedAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;
}

