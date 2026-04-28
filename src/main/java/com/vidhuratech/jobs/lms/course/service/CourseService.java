package com.vidhuratech.jobs.lms.course.service;

import com.vidhuratech.jobs.lms.course.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseService {

    CourseResponseDTO create(CourseRequestDTO dto);

    CourseResponseDTO update(Long id, CourseRequestDTO dto);

    CourseResponseDTO getById(Long id);

    Page<CourseResponseDTO> search(CourseSearchFilterDTO filter, Pageable pageable);

    void publish(Long id);

    void archive(Long id);

    void softDelete(Long id);

    BulkCourseResponse bulkCreate(List<CourseRequestDTO> list);
}