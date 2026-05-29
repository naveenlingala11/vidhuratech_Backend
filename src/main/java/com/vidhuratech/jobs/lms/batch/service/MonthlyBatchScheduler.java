package com.vidhuratech.jobs.lms.batch.service;

import com.vidhuratech.jobs.lms.course.entity.Course;
import com.vidhuratech.jobs.lms.course.entity.CourseStatus;
import com.vidhuratech.jobs.lms.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyBatchScheduler {

    private final CourseRepository courseRepository;
    private final BatchAutomationService batchAutomationService;

    @Scheduled(cron = "0 0 1 1 * *")
    @Transactional
    public void createMonthlyBatches() {
        List<Course> courses =
                courseRepository.findByStatusAndActiveTrueAndAutoMonthlyBatchEnabledTrue(
                        CourseStatus.PUBLISHED
                );

        courses.forEach(batchAutomationService::createCurrentMonthBatchIfMissing);
    }
}