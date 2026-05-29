package com.vidhuratech.jobs.publicpractice.repository;

import com.vidhuratech.jobs.publicpractice.entity.PublicChallengeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicChallengeAttemptRepository
        extends JpaRepository<PublicChallengeAttempt, Long> {

    List<PublicChallengeAttempt> findTop200ByOrderBySubmittedAtDesc();

    List<PublicChallengeAttempt> findByChallengeIdOrderBySubmittedAtDesc(Long challengeId);

    long countByChallengeId(Long challengeId);
}