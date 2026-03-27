package com.vidhuratech.jobs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "jobs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"title", "company_id", "apply_link"})
)
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String role;

    // ✅ FIX: Make it NOT optional for FK stability
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private String location;
    private String experience;
    private String jobType;
    private String category;
    private String employmentType;
    private String salary;
    private String source;
    private Boolean remote = false;

    @Column(length = 10000)
    private String description;

    @Column(name = "apply_link", length = 1000)
    private String applyLink;

    // ✅ FIX: No cascade here (important)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "job_skills",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    private LocalDateTime postedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ── TRANSIENT (OK as is) ──
    @Transient
    private String companyName;

    @Transient
    private String skillsCsv;

    // ── CLEAN METHODS ──

// ✅ ADD THIS (VERY IMPORTANT)

    // ✅ for scraper
    public void setCompanyName(String name) {
        this.companyName = name;
    }

    // ✅ for service
    public String getCompanyString() {
        if (company != null) return company.getName();
        return companyName;
    }

    // ✅ for scraper
    public void setSkillsCsv(String csv) {
        this.skillsCsv = csv;
    }

    // ✅ for service
    public String getSkillsCsv() {
        return skillsCsv;
    }
}