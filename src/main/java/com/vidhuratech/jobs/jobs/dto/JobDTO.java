package com.vidhuratech.jobs.jobs.dto;

import lombok.Data;
import java.util.Set;

@Data
public class JobDTO {

    private Long id;
    private String title;
    private String company;
    private String location;
    private String experience;
    private String jobType;
    private String category;
    private String description;
    private Set<String> skills;
    private Boolean remote;
    private String applyLink;
}