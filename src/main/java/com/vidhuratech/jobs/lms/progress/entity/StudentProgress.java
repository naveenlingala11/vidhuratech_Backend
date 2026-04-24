package com.vidhuratech.jobs.lms.progress.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "student_progress",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "studentEmail", "batchId", "sessionId"
                })
        })
public class StudentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentEmail;

    private Long batchId;

    private Long sessionId;

    private Boolean completed = false;

    private Boolean lastWatched = false;

    private Long lastWatchedTime; // optional (seconds)
}