package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.PseudoCodeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PseudoCodeAttemptRepository extends JpaRepository<PseudoCodeAttempt, Long> {

    List<PseudoCodeAttempt> findByChallengeIdOrderBySubmittedAtDesc(Long challengeId);

    List<PseudoCodeAttempt> findByStudentIdOrderBySubmittedAtDesc(Long studentId);

    List<PseudoCodeAttempt> findByChallengeIdAndStudentIdOrderBySubmittedAtDesc(
            Long challengeId,
            Long studentId
    );
}