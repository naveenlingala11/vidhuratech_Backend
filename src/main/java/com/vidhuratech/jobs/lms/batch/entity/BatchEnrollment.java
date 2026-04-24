package com.vidhuratech.jobs.lms.batch.entity;

import com.vidhuratech.jobs.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "batch_enrollments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"batch_id", "student_id"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    private LocalDateTime enrolledAt;

    private Boolean active = true;
}