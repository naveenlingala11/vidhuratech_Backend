package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.AssessmentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {

    List<AssessmentAttempt> findByAssessmentId(Long assessmentId);

    List<AssessmentAttempt> findByStudentId(Long studentId);

    Optional<AssessmentAttempt> findByAssessmentIdAndStudentId(Long assessmentId, Long studentId);

    List<AssessmentAttempt> findByAssessmentIdAndStudentIdOrderByIdDesc(Long assessmentId, Long studentId);

    @Query("""
        SELECT at
        FROM AssessmentAttempt at
        LEFT JOIN FETCH at.student
        LEFT JOIN FETCH at.assessment a
        WHERE a.id = :assessmentId
        ORDER BY at.id DESC
    """)
    List<AssessmentAttempt> findDetailedByAssessmentId(Long assessmentId);

    @Query("""
        SELECT at
        FROM AssessmentAttempt at
        LEFT JOIN FETCH at.student
        LEFT JOIN FETCH at.assessment a
        WHERE a.id = :assessmentId
        AND at.id = :attemptId
    """)
    Optional<AssessmentAttempt> findDetailedByAssessmentIdAndAttemptId(Long assessmentId, Long attemptId);

    @Modifying
    @Query("DELETE FROM AssessmentAttempt at WHERE at.assessment.id = :assessmentId")
    void deleteByAssessmentId(Long assessmentId);
}