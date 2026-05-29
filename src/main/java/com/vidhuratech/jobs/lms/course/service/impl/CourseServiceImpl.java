package com.vidhuratech.jobs.lms.course.service.impl;

import com.vidhuratech.jobs.common.exception.*;
import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.lms.batch.dto.BatchRequestDTO;
import com.vidhuratech.jobs.lms.batch.entity.BatchStatus;
import com.vidhuratech.jobs.lms.batch.service.AdminBatchService;
import com.vidhuratech.jobs.lms.batch.service.BatchAutomationService;
import com.vidhuratech.jobs.lms.batch.service.BatchService;
import com.vidhuratech.jobs.lms.course.dto.*;
import com.vidhuratech.jobs.lms.course.entity.Course;
import com.vidhuratech.jobs.lms.course.entity.CourseStatus;
import com.vidhuratech.jobs.lms.course.mapper.CourseMapper;
import com.vidhuratech.jobs.lms.course.repository.CourseRepository;
import com.vidhuratech.jobs.lms.course.service.CourseService;
import com.vidhuratech.jobs.lms.course.specification.CourseSpecification;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository repository;
    private final CourseMapper mapper;
    private final ActivityNotificationService notificationService;
    private final BatchAutomationService batchAutomationService;
    private final UserRepository userRepository;

    @Override
    public CourseResponseDTO create(CourseRequestDTO dto) {

        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new BusinessValidationException("Course code is required");
        }

        String code = dto.getCode().toUpperCase().trim();

        if (repository.existsByCode(code)) {
            throw new DuplicateResourceException("Course already exists: " + code);
        }

        dto.setCode(code);

        Course course = mapper.toEntity(dto);
        applyDefaultTrainer(course, dto.getDefaultTrainerId());
        course.setStatus(CourseStatus.DRAFT);

        Course saved = repository.save(course);
        notificationService.notifyAdmins(
                "Course created",
                "Course created: " + saved.getTitle(),
                "COURSE_CREATED",
                "/dashboard/lms/courses"
        );
        return mapper.toResponse(saved);
    }

    @Override
    public BulkCourseResponse bulkCreate(List<CourseRequestDTO> list) {

        List<String> duplicates = new ArrayList<>();
        int success = 0;
        int failed = 0;
        for (CourseRequestDTO dto : list) {
            try {
                if (dto.getCode() == null || dto.getCode().isBlank()) {
                    failed++;
                    continue;
                }
                String code = dto.getCode().toUpperCase().trim();
                if (repository.existsByCode(code)) {
                    duplicates.add(code);
                    failed++;
                    continue;
                }
                dto.setCode(code);
                Course course = mapper.toEntity(dto);
                // ✅ AUTO PUBLISH
                course.setStatus(CourseStatus.DRAFT);
                Course saved = repository.save(course);
                success++;

            } catch (Exception e) {
                failed++;
            }
        }

        return BulkCourseResponse.builder()
                .successCount(success)
                .failedCount(failed)
                .duplicateCodes(duplicates)
                .build();
    }

    @Override
    public CourseResponseDTO update(Long id, CourseRequestDTO dto) {
        Course course = getEntity(id);

        mapper.updateEntity(course, dto);
        applyDefaultTrainer(course, dto.getDefaultTrainerId());
        return mapper.toResponse(repository.save(course));
    }

    @Override
    public CourseResponseDTO getById(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Override
    public Page<CourseResponseDTO> search(CourseSearchFilterDTO filter, Pageable pageable) {
        return repository.findAll(
                CourseSpecification.withFilters(filter),
                pageable
        ).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public CourseResponseDTO publish(Long id) {
        Course course = getEntity(id);

        boolean alreadyPublished = course.getStatus() == CourseStatus.PUBLISHED;

        course.setStatus(CourseStatus.PUBLISHED);
        course.setActive(true);

        Course saved = repository.save(course);

        batchAutomationService.createCurrentMonthBatchIfMissing(saved);

        if (!alreadyPublished) {
            notificationService.notifyAdmins(
                    "Course published",
                    "Course published: " + saved.getTitle(),
                    "COURSE_PUBLISHED",
                    "/dashboard/lms/courses"
            );
        }

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CourseResponseDTO unpublish(Long id) {
        Course course = getEntity(id);

        course.setStatus(CourseStatus.DRAFT);
        Course saved = repository.save(course);

        notificationService.notifyAdmins(
                "Course unpublished",
                "Course unpublished: " + saved.getTitle(),
                "COURSE_UNPUBLISHED",
                "/dashboard/lms/courses"
        );

        return mapper.toResponse(saved);
    }

    @Override
    public void archive(Long id) {
        Course course = getEntity(id);

        course.setStatus(CourseStatus.ARCHIVED);
        repository.save(course);
    }

    @Override
    public void softDelete(Long id) {
        Course course = getEntity(id);

        course.setActive(false);
        repository.save(course);
    }

    @Override
    public List<CourseResponseDTO> getFeaturedCourses() {
        return repository
                .findByActiveTrueAndStatusAndFeaturedOnHomeTrueOrderByFeaturedRankAscIdDesc(
                        CourseStatus.PUBLISHED
                )
                .stream()
                .limit(3)
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public CourseResponseDTO updateThumbnail(Long id, String thumbnailUrl) {
        Course course = getEntity(id);
        course.setThumbnailUrl(thumbnailUrl);
        return mapper.toResponse(repository.save(course));
    }

    private Course getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    private void applyDefaultTrainer(Course course, Long trainerId) {
        if (trainerId == null) {
            course.setDefaultTrainer(null);
            return;
        }

        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

        if (trainer.getRole() != UserRole.TRAINER) {
            throw new BusinessValidationException("Selected user is not a trainer");
        }

        course.setDefaultTrainer(trainer);
    }
}