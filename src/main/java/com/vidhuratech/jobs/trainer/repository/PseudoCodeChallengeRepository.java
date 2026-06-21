package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.PseudoCodeChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PseudoCodeChallengeRepository extends JpaRepository<PseudoCodeChallenge, Long> {

    List<PseudoCodeChallenge> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);

    List<PseudoCodeChallenge> findByBatchIdInAndActiveTrueOrderByCreatedAtDesc(List<Long> batchIds);

    long countByBatchIdInAndActiveTrue(List<Long> batchIds);

    List<PseudoCodeChallenge> findByActiveTrueOrderByCreatedAtDesc();

    @Query("""
        SELECT DISTINCT c
        FROM PseudoCodeChallenge c
        LEFT JOIN FETCH c.testCases
        WHERE c.active = true AND c.publicVisible = true
        ORDER BY c.publishedAt DESC NULLS LAST, c.id DESC
    """)
    List<PseudoCodeChallenge> findByActiveTrueAndPublicVisibleTrueOrderByPublishedAtDesc();

    List<PseudoCodeChallenge> findAllByOrderByIdDesc();

    @Query("""
        SELECT DISTINCT c
        FROM PseudoCodeChallenge c
        LEFT JOIN FETCH c.testCases
        ORDER BY c.id DESC
    """)
    List<PseudoCodeChallenge> findAllPublicPracticeCandidates();

    @Query("""
    SELECT DISTINCT c
    FROM PseudoCodeChallenge c
    LEFT JOIN FETCH c.testCases
    WHERE c.id = :id
""")
    Optional<PseudoCodeChallenge> findPublicPracticeCandidateById(Long id);
}