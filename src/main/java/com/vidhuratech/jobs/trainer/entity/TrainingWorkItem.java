package com.vidhuratech.jobs.trainer.entity;

import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "training_work_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingWorkItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainer_id")
    private User trainer;

    @Enumerated(EnumType.STRING)
    private TrainingWorkType type;

    private String title;

    @Column(length = 3000)
    private String description;

    private LocalDateTime dueAt;

    private Integer totalMarks;

    private Boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();
}

