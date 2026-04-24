package com.vidhuratech.jobs.lms.batch.entity;

import com.vidhuratech.jobs.lms.course.entity.Course;
import com.vidhuratech.jobs.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "batches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private User trainer;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private BatchStatus status;

    private Boolean active = true;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL)
    private List<BatchEnrollment> enrollments;
}