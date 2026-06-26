package com.vidhuratech.jobs.publicpractice.repository;

import com.vidhuratech.jobs.publicpractice.entity.PublicAssessmentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicAssessmentAttemptRepository
        extends JpaRepository<PublicAssessmentAttempt, Long> {

    List<PublicAssessmentAttempt> findTop200ByOrderBySubmittedAtDesc();

    List<PublicAssessmentAttempt> findByAssessmentIdOrderBySubmittedAtDesc(Long assessmentId);

    long countByAssessmentId(Long assessmentId);

    @org.springframework.data.jpa.repository.Query("SELECT a.assessmentId, COUNT(a) FROM PublicAssessmentAttempt a WHERE a.assessmentId IN :assessmentIds GROUP BY a.assessmentId")
    List<Object[]> countAttemptsForAssessments(@org.springframework.data.repository.query.Param("assessmentIds") List<Long> assessmentIds);
}