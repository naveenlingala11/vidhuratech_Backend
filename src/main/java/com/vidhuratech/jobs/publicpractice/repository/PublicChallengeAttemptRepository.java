package com.vidhuratech.jobs.publicpractice.repository;

import com.vidhuratech.jobs.publicpractice.entity.PublicChallengeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicChallengeAttemptRepository
        extends JpaRepository<PublicChallengeAttempt, Long> {

    List<PublicChallengeAttempt> findTop200ByOrderBySubmittedAtDesc();

    List<PublicChallengeAttempt> findByChallengeIdOrderBySubmittedAtDesc(Long challengeId);

    long countByChallengeId(Long challengeId);

    @Query("""
        select a from PublicChallengeAttempt a
        where a.challengeId = :challengeId
        order by a.score desc, a.totalExecutionTimeMs asc, a.submittedAt asc
        """)
    List<PublicChallengeAttempt> leaderboardByChallenge(@Param("challengeId") Long challengeId);

    @Query("""
        select a from PublicChallengeAttempt a
        where a.submittedAt between :start and :end
        order by a.score desc, a.totalExecutionTimeMs asc, a.submittedAt asc
        """)
    List<PublicChallengeAttempt> weeklyLeaderboard(
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end
    );

    @Query("""
        select a from PublicChallengeAttempt a
        where a.submittedAt between :start and :end
        order by a.submittedAt desc
        """)
    List<PublicChallengeAttempt> leaderboardBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}