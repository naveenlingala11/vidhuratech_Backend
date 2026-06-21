package com.vidhuratech.jobs.trainer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "training_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "trainer_email")
    private String trainerEmail;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TrainingContentType type;

    private String title;

    @Column(length = 2000)
    private String description;

    private String fileName;

    private String fileType;

    @Column(name = "file_data", columnDefinition = "bytea")
    private byte[] fileData;

    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String jsonData;

    @Column(columnDefinition = "TEXT")
    private String links;
}