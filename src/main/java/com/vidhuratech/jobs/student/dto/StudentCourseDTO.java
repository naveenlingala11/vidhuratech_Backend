package com.vidhuratech.jobs.student.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentCourseDTO {

    private Long courseId;
    private String courseName;

    private Long batchId;
    private String batchName;

    private Integer progress;
}