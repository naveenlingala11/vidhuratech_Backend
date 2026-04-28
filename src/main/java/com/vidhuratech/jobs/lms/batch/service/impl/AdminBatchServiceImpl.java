package com.vidhuratech.jobs.lms.batch.service.impl;

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
                .map(batch -> Map.of(
                        "id", batch.getId(),
                        "name", batch.getName(),

                        "courseId", batch.getCourse() != null ? batch.getCourse().getId() : null,
                        "courseName", batch.getCourse() != null ? batch.getCourse().getTitle() : "-",

                        "trainerId", batch.getTrainer() != null ? batch.getTrainer().getId() : null,
                        "trainerName", batch.getTrainer() != null ? batch.getTrainer().getName() : "-",

                        "status", batch.getStatus(),
                        "studentCount", enrollmentRepository.countByBatchId(batch.getId())
                ))
                .toList();

        return Map.of(
                "content", content,
                "totalElements", batchPage.getTotalElements(),
                "totalPages", batchPage.getTotalPages(),
                "page", batchPage.getNumber()
        );
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

        return batchRepository.save(batch);
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

        return batchRepository.save(batch);
    }

    @Override
    public void deleteBatch(Long id) {
        batchRepository.deleteById(id);
    }

}
