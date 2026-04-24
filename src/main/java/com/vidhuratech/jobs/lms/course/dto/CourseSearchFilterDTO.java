package com.vidhuratech.jobs.lms.course.dto;

import com.vidhuratech.jobs.lms.course.entity.CourseLevel;
import com.vidhuratech.jobs.lms.course.entity.CourseStatus;
import lombok.Data;

@Data
public class CourseSearchFilterDTO {

    private String keyword;
    private CourseLevel level;
    private CourseStatus status;
    private Boolean active;
}