package com.vidhuratech.jobs.lms.course.repository;

import com.vidhuratech.jobs.lms.course.entity.CourseTrainerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseTrainerAssignmentRepository
        extends JpaRepository<CourseTrainerAssignment, Long> {

    List<CourseTrainerAssignment> findByActiveTrueOrderByAssignedAtDesc();

    List<CourseTrainerAssignment> findByCourseIdAndActiveTrue(Long courseId);

    Optional<CourseTrainerAssignment> findFirstByCourseIdAndActiveTrueOrderByAssignedAtDesc(Long courseId);

    Optional<CourseTrainerAssignment> findByCourseIdAndTrainerIdAndActiveTrue(Long courseId, Long trainerId);

    @Query("""
        SELECT DISTINCT a
        FROM CourseTrainerAssignment a
        LEFT JOIN FETCH a.course
        LEFT JOIN FETCH a.trainer
        WHERE a.active = true
        ORDER BY a.assignedAt DESC
    """)
    List<CourseTrainerAssignment> findActiveDetailed();

    @Query("""
    SELECT DISTINCT a
    FROM CourseTrainerAssignment a
    LEFT JOIN FETCH a.course c
    LEFT JOIN FETCH a.trainer t
    WHERE a.active = true
    AND t.email = :email
    ORDER BY a.assignedAt DESC
""")
    List<CourseTrainerAssignment> findActiveDetailedByTrainerEmail(String email);
}