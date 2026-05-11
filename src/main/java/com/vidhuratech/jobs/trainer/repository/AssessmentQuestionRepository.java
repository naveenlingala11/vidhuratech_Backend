package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentQuestionRepository
        extends JpaRepository<AssessmentQuestion, Long> {

    List<AssessmentQuestion> findByAssessmentId(Long assessmentId);
}