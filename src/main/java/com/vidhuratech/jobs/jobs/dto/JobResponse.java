package com.vidhuratech.jobs.jobs.dto;

import com.vidhuratech.jobs.jobs.entity.Job;
import com.vidhuratech.jobs.jobs.entity.Skill;
import lombok.Data;

import lombok.Getter;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Data
public class JobResponse {

    private Long id;
    private String title;
    private String location;
    private String companyName;
    private String applyLink;

    // 🔥 NEW
    private String experience;
    private String jobType;
    private String category;
    private String salary;
    private String description;
    private Boolean remote;
    private String source;
    private String postedAt;
    private Set<String> skills;

    public JobResponse(Job job) {
        this.id = job.getId();
        this.title = job.getTitle();
        this.location = job.getLocation();
        this.companyName = job.getCompany() != null
                ? job.getCompany().getName()
                : "Unknown";
        this.applyLink = job.getApplyLink();

        // 🔥 NEW FIELDS
        this.experience = job.getExperience();
        this.jobType = job.getJobType();
        this.category = job.getCategory();
        this.salary = job.getSalary();
        this.description = job.getDescription();
        this.remote = job.getRemote();
        this.source = job.getSource();

        this.postedAt = job.getPostedAt() != null
                ? job.getPostedAt().toString()
                : null;

        this.skills = job.getSkills() != null
                ? job.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet())
                : Set.of();
    }
}