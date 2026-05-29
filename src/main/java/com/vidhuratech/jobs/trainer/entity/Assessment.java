package com.vidhuratech.jobs.trainer.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assessments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 5000)
    private String description;

    private Integer totalMarks;
    private Integer durationMinutes;

    @Builder.Default
    private Boolean active = true;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    @JsonIgnoreProperties({"password", "createdAt", "updatedAt"})
    private User trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    @JsonIgnoreProperties({"enrollments"})
    private Batch batch;

    @Builder.Default
    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AssessmentQuestion> questions = new ArrayList<>();

    private String companyName;
    private String skill;

    @Builder.Default
    private Boolean publicVisible = false;

    @Builder.Default
    private String publicAccessLevel = "LEAD_REQUIRED";

    @Builder.Default
    private Integer publicAttemptLimit = 1;

    private LocalDateTime publishedAt;
    private Long publishedByUserId;
}