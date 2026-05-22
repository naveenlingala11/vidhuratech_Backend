package com.vidhuratech.jobs.trainer.entity;

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
public class PseudoCodeChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long batchId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String problemStatement;

    @Column(columnDefinition = "TEXT")
    private String constraintsText;

    @Column(columnDefinition = "TEXT")
    private String inputFormat;

    @Column(columnDefinition = "TEXT")
    private String outputFormat;

    private Integer totalMarks;

    private Integer passPercentage;

    private Integer durationMinutes;

    private String trainerEmail;

    private Boolean active;

    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PseudoCodeRule> rules = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PseudoCodeTestCase> testCases = new ArrayList<>();
}