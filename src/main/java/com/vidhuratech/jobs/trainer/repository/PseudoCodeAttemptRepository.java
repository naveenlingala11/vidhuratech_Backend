package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.PseudoCodeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PseudoCodeAttemptRepository extends JpaRepository<PseudoCodeAttempt, Long> {

    List<PseudoCodeAttempt> findByChallengeIdOrderBySubmittedAtDesc(Long challengeId);

    long countByChallengeId(Long challengeId);

    List<PseudoCodeAttempt> findByStudentIdOrderBySubmittedAtDesc(Long studentId);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM PseudoCodeAttempt a LEFT JOIN FETCH a.challenge WHERE a.student.id = :studentId ORDER BY a.submittedAt DESC")
    List<PseudoCodeAttempt> findByStudentIdWithChallengeEagerly(@org.springframework.data.repository.query.Param("studentId") Long studentId);

    List<PseudoCodeAttempt> findByChallengeIdAndStudentIdOrderBySubmittedAtDesc(
            Long challengeId,
            Long studentId
    );

    List<PseudoCodeAttempt> findByChallengeTrainerEmailOrderBySubmittedAtDesc(String trainerEmail);
}