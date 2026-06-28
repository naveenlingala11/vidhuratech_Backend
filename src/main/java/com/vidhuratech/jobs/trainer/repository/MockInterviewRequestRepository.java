package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.MockInterviewRequest;
import com.vidhuratech.jobs.trainer.entity.MockInterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockInterviewRequestRepository extends JpaRepository<MockInterviewRequest, Long> {

    List<MockInterviewRequest> findByStudentEmailOrderByCreatedAtDesc(String studentEmail);

    List<MockInterviewRequest> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);

    Long countByTrainerEmailAndStatus(String trainerEmail, MockInterviewStatus status);

    @org.springframework.data.jpa.repository.Query("""
        SELECT r FROM MockInterviewRequest r
        LEFT JOIN r.trainer t
        LEFT JOIN r.student s
        WHERE LOWER(r.hostEmail) = LOWER(:email)
        OR LOWER(r.candidateEmail) = LOWER(:email)
        OR (t IS NOT NULL AND LOWER(t.email) = LOWER(:email))
        OR (s IS NOT NULL AND LOWER(s.email) = LOWER(:email))
        OR (r.invitedEmails IS NOT NULL AND LOWER(r.invitedEmails) LIKE CONCAT('%', LOWER(:email), '%'))
        OR EXISTS (
            SELECT h FROM MockInterviewJoinHistory h
            WHERE h.mockInterview = r
            AND LOWER(h.joinedByEmail) = LOWER(:email)
        )
        ORDER BY r.createdAt DESC
    """)
    List<MockInterviewRequest> findAllUserSessions(String email);
}

