package com.vidhuratech.jobs.lms.course.service.impl;

import com.vidhuratech.jobs.common.exception.*;
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

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository repository;
    private final CourseMapper mapper;

    @Override
    public CourseResponseDTO create(CourseRequestDTO dto) {
        if (repository.existsByCode(dto.getCode().toUpperCase())) {
            throw new DuplicateResourceException("Course code already exists");
        }

        Course course = mapper.toEntity(dto);
        return mapper.toResponse(repository.save(course));
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