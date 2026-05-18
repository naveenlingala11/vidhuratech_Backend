package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssessmentRepository
        extends JpaRepository<Assessment, Long> {

    List<Assessment> findByTrainerEmail(
            String email
    );

    @Query("""
    SELECT DISTINCT a
    FROM Assessment a
    LEFT JOIN FETCH a.questions q
    LEFT JOIN FETCH a.batch b
    WHERE b.id IN :batchIds
    AND a.active = true
    AND (
        a.startTime IS NULL
        OR a.startTime <= :now
    )
    AND (
        a.endTime IS NULL
        OR a.endTime >= :now
    )
    ORDER BY a.id DESC
""")
    List<Assessment>
    findActiveAssessmentsForStudent(
            List<Long> batchIds,
            LocalDateTime now
    );

    @Query("""
    SELECT a
    FROM Assessment a
    LEFT JOIN FETCH a.questions
    LEFT JOIN FETCH a.batch
    WHERE a.id = :id
""")
    Optional<Assessment>findDetailedAssessment(Long id);
}