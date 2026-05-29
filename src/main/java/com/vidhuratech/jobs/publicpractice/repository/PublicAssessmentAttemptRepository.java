package com.vidhuratech.jobs.publicpractice.repository;

import com.vidhuratech.jobs.publicpractice.entity.PublicAssessmentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicAssessmentAttemptRepository
        extends JpaRepository<PublicAssessmentAttempt, Long> {

    List<PublicAssessmentAttempt> findTop200ByOrderBySubmittedAtDesc();

    List<PublicAssessmentAttempt> findByAssessmentIdOrderBySubmittedAtDesc(Long assessmentId);

    long countByAssessmentId(Long assessmentId);
}