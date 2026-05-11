package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.MockInterviewRequest;
import com.vidhuratech.jobs.trainer.entity.MockInterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockInterviewRequestRepository extends JpaRepository<MockInterviewRequest, Long> {

    List<MockInterviewRequest> findByStudentEmailOrderByCreatedAtDesc(String studentEmail);

    List<MockInterviewRequest> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);

    Long countByTrainerEmailAndStatus(String trainerEmail, MockInterviewStatus status);
}

