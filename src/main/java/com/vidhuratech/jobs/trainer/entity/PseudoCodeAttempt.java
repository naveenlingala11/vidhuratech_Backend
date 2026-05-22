package com.vidhuratech.jobs.trainer.entity;

import com.vidhuratech.jobs.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PseudoCodeAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id")
    private PseudoCodeChallenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    private String language; // JAVA, PYTHON

    @Column(columnDefinition = "TEXT")
    private String sourceCode;

    @Column(columnDefinition = "TEXT")
    private String compileError;

    private Integer score;

    private Integer totalMarks;

    private Integer percentage;

    private String status; // PASS, FAIL

    private Boolean allTestsPassed;

    private LocalDateTime submittedAt;

    @Builder.Default
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PseudoCodeAttemptOutput> outputs = new ArrayList<>();
}