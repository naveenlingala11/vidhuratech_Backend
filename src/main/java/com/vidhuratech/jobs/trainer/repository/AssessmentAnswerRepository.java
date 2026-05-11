package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.AssessmentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentAnswerRepository
        extends JpaRepository<AssessmentAnswer, Long> {
}