package com.vidhuratech.jobs.lms.course.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulkCourseResponse {

    private int successCount;
    private int failedCount;
    private List<String> duplicateCodes;
}