package com.vidhuratech.jobs.jobs.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "scraper_configs")
@Data
public class ScraperConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String company;
    private String type;
    private String url;

    private boolean active = true;

    @Column(nullable = false)
    private int successCount = 0;

    @Column(nullable = false)
    private int failCount = 0;
}
