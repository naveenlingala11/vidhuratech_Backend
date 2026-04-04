package com.vidhuratech.jobs.certificate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    private String id;

    private String name;
    private String course;
    private String email;

    private LocalDateTime issuedAt;
}