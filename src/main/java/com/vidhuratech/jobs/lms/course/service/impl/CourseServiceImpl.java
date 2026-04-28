package com.vidhuratech.jobs.lms.course.service.impl;

import com.vidhuratech.jobs.common.exception.*;
import com.vidhuratech.jobs.lms.batch.dto.BatchRequestDTO;
import com.vidhuratech.jobs.lms.batch.entity.BatchStatus;
import com.vidhuratech.jobs.lms.batch.service.AdminBatchService;
import com.vidhuratech.jobs.lms.batch.service.BatchService;
import com.vidhuratech.jobs.lms.course.dto.*;
import com.vidhuratech.jobs.lms.course.entity.Course;
import com.vidhuratech.jobs.lms.course.entity.CourseStatus;
import com.vidhuratech.jobs.lms.course.mapper.CourseMapper;
import com.vidhuratech.jobs.lms.course.repository.CourseRepository;
import com.vidhuratech.jobs.lms.course.service.CourseService;
import com.vidhuratech.jobs.lms.course.specification.CourseSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository repository;
    private final CourseMapper mapper;
    private final AdminBatchService adminBatchService;

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

        // ✅ AUTO PUBLISH (VERY IMPORTANT)
        course.setStatus(CourseStatus.PUBLISHED);

        Course saved = repository.save(course);

        // 🔥 AUTO BATCH CREATE
        createAutoBatch(saved.getId(), saved.getCode(), saved.getTitle());
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
                course.setStatus(CourseStatus.PUBLISHED);
                Course saved = repository.save(course);
                // 🔥 ADD THIS
                createAutoBatch(saved.getId(), saved.getCode(), saved.getTitle());
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

    private void createAutoBatch(Long courseId, String courseCode, String courseTitle) {

        try {
            BatchRequestDTO dto = new BatchRequestDTO();

            dto.setCourseId(courseId);
            dto.setTrainerId(1L);
            dto.setName("Batch - " + courseTitle);

            // 🔥 ONLY PYTHON + DS ACTIVE
            if ("PYTHON_DS".equalsIgnoreCase(courseCode)) {

                dto.setStartDate(java.time.LocalDate.of(2026, 5, 2));
                dto.setEndDate(java.time.LocalDate.of(2026, 8, 2));
                dto.setStatus(BatchStatus.ACTIVE);

            } else {

                dto.setStartDate(java.time.LocalDate.now().plusMonths(1));
                dto.setEndDate(java.time.LocalDate.now().plusMonths(4));
                dto.setStatus(BatchStatus.COMPLETED);
            }

            adminBatchService.createBatch(dto);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public CourseResponseDTO update(Long id, CourseRequestDTO dto) {
        Course course = getEntity(id);

        mapper.updateEntity(course, dto);

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
    public void publish(Long id) {
        Course course = getEntity(id);

        if (course.getStatus() == CourseStatus.ARCHIVED) {
            throw new BusinessValidationException("Archived course cannot be published");
        }

        course.setStatus(CourseStatus.PUBLISHED);
        repository.save(course);
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

    private Course getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }
}