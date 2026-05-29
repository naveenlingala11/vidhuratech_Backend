package com.vidhuratech.jobs.lms.batch.service;

import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.entity.BatchStatus;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;
import com.vidhuratech.jobs.lms.course.entity.Course;
import com.vidhuratech.jobs.lms.course.entity.CourseTrainerAssignment;
import com.vidhuratech.jobs.lms.course.repository.CourseTrainerAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BatchAutomationService {

    private final BatchRepository batchRepository;
    private final CourseTrainerAssignmentRepository trainerAssignmentRepository;

    @Transactional
    public void createCurrentMonthBatchIfMissing(Course course) {
        if (!Boolean.TRUE.equals(course.getAutoMonthlyBatchEnabled())) {
            return;
        }

        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        Integer duration = course.getMonthlyBatchDurationMonths() == null
                ? 3
                : course.getMonthlyBatchDurationMonths();

        LocalDate endDate = startDate.plusMonths(duration).minusDays(1);

        String month = now.getMonth().name().substring(0, 3);
        String batchCode = course.getCode() + "-" + month + "-" + now.getYear();

        if (batchRepository.existsByCourseIdAndCode(course.getId(), batchCode)) {
            return;
        }

        Batch batch = Batch.builder()
                .course(course)
                .name(course.getTitle() + " - " + month + " " + now.getYear())
                .code(batchCode)
                .startDate(startDate)
                .endDate(endDate)
                .status(BatchStatus.ACTIVE)
                .active(true)
                .build();

        Batch savedBatch = batchRepository.save(batch);

        if (course.getDefaultTrainer() != null) {
            CourseTrainerAssignment assignment = CourseTrainerAssignment.builder()
                    .course(course)
                    .batch(savedBatch)
                    .trainer(course.getDefaultTrainer())
                    .active(true)
                    .assignedAt(LocalDateTime.now())
                    .build();

            trainerAssignmentRepository.save(assignment);
        }
    }
}