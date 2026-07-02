package com.vidhuratech.jobs.lms.batch.service.impl;

import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.lms.batch.dto.BatchRequestDTO;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;
import com.vidhuratech.jobs.lms.batch.service.AdminBatchService;
import com.vidhuratech.jobs.lms.batch.spec.BatchSpecification;
import com.vidhuratech.jobs.lms.course.entity.Course;
import com.vidhuratech.jobs.lms.course.repository.CourseRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminBatchServiceImpl implements AdminBatchService {

    private final BatchRepository batchRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final BatchEnrollmentRepository enrollmentRepository;
    private final ActivityNotificationService notificationService;


    @Override
    public Map<String, Object> getAllBatches(
            String keyword,
            String status,
            Long courseId,
            Long trainerId,
            int page,
            int size
    ) {

        var pageable = PageRequest.of(page, size, Sort.by("id").descending());

        var spec = BatchSpecification.search(
                keyword,
                status,
                courseId,
                trainerId
        );

        Page<Batch> batchPage = batchRepository.findAll(spec, pageable);

        var content = batchPage.getContent().stream()
                .map(batch -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", batch.getId());
                    map.put("name", batch.getName());
                    map.put("courseId", batch.getCourse() != null ? batch.getCourse().getId() : null);
                    map.put("courseName", batch.getCourse() != null ? batch.getCourse().getTitle() : "-");
                    map.put("trainerId", batch.getTrainer() != null ? batch.getTrainer().getId() : null);
                    map.put("trainerName", batch.getTrainer() != null ? batch.getTrainer().getName() : "-");
                    map.put("status", batch.getStatus());
                    map.put("studentCount", enrollmentRepository.countByBatchId(batch.getId()));
                    return map;
                })
                .toList();

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", content);
        response.put("totalElements", batchPage.getTotalElements());
        response.put("totalPages", batchPage.getTotalPages());
        response.put("page", batchPage.getNumber());
        return response;
    }

    @Override
    public Object createBatch(BatchRequestDTO dto) {

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User trainer = userRepository.findById(dto.getTrainerId())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        Batch batch = Batch.builder()
                .name(dto.getName())
                .course(course)
                .trainer(trainer)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(dto.getStatus())
                .active(true)
                .build();

        Batch saved = batchRepository.save(batch);

        notificationService.notifyTrainer(
                trainer,
                "New batch assigned",
                "You were assigned to batch: " + saved.getName(),
                "BATCH_ASSIGNED",
                "/dashboard/trainer/batches"
        );

        notificationService.notifyAdmins(
                "Batch created",
                saved.getName() + " created for " + course.getTitle(),
                "BATCH_CREATED",
                "/dashboard/admin/batches"
        );

        return saved;
    }

    @Override
    public Object updateBatch(Long id, BatchRequestDTO dto) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User trainer = userRepository.findById(dto.getTrainerId())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        batch.setName(dto.getName());
        batch.setCourse(course);
        batch.setTrainer(trainer);
        batch.setStartDate(dto.getStartDate());
        batch.setEndDate(dto.getEndDate());
        batch.setStatus(dto.getStatus());

        Batch saved = batchRepository.save(batch);

        notificationService.notifyTrainer(
                trainer,
                "Batch updated",
                "Batch details updated: " + saved.getName(),
                "BATCH_UPDATED",
                "/dashboard/trainer/batches"
        );

        return saved;    }

    @Override
    public void deleteBatch(Long id) {
        batchRepository.deleteById(id);
    }

}
