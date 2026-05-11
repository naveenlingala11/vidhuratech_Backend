package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.TrainingSubmission;
import com.vidhuratech.jobs.trainer.entity.TrainingSubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TrainingSubmissionRepository extends JpaRepository<TrainingSubmission, Long> {

    Optional<TrainingSubmission> findByWorkItemIdAndStudentEmail(Long workItemId, String studentEmail);

    @Query("""
        SELECT s
        FROM TrainingSubmission s
        WHERE s.workItem.batch.trainer.email = :trainerEmail
        ORDER BY s.submittedAt DESC
    """)
    List<TrainingSubmission> findByTrainerEmail(String trainerEmail);

    List<TrainingSubmission> findByStudentEmailOrderBySubmittedAtDesc(String studentEmail);

    @Query("""
        SELECT COUNT(s)
        FROM TrainingSubmission s
        WHERE s.workItem.trainer.email = :trainerEmail
        AND s.status = :status
    """)
    Long countByTrainerEmailAndStatus(String trainerEmail, TrainingSubmissionStatus status);
}
