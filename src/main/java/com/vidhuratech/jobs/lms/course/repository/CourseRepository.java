package com.vidhuratech.jobs.lms.course.repository;

import com.vidhuratech.jobs.lms.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    boolean existsByCode(String code);

    Optional<Course> findByCode(String code);
}