package com.vidhuratech.jobs.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseRevenueDto {

    private String course;
    private Double revenue;
    private Long count;
}