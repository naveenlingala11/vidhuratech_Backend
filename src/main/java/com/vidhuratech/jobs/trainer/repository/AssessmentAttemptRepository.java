package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.AssessmentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssessmentAttemptRepository
        extends JpaRepository<AssessmentAttempt, Long> {

    List<AssessmentAttempt> findByAssessmentId(Long assessmentId);

    List<AssessmentAttempt> findByStudentId(Long studentId);

    Optional<AssessmentAttempt> findByAssessmentIdAndStudentId(
            Long assessmentId,
            Long studentId
    );
}