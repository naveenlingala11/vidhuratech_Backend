package com.vidhuratech.jobs.trainer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curriculum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long batchId;

    private String trainerEmail;

    @Column(columnDefinition = "TEXT")
    private String jsonData;
}