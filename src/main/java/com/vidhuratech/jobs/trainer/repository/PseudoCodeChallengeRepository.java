package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.PseudoCodeChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PseudoCodeChallengeRepository extends JpaRepository<PseudoCodeChallenge, Long> {

    List<PseudoCodeChallenge> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);

    // Lightweight listing query: returns all challenge data + counts in ONE query (no N+1)
    @Query(value = """
        SELECT c.id, c.batch_id, c.title, c.problem_statement, c.duration_minutes,
               c.total_marks, c.pass_percentage, c.active, c.created_at,
               c.challenge_group_id, c.challenge_group_title, c.company_name,
               c.skill, c.asked_year, c.public_visible, c.public_access_level,
               c.public_attempt_limit, c.published_at, c.hint_text, c.difficulty_level,
               COALESCE((SELECT COUNT(*) FROM pseudo_code_rule r WHERE r.challenge_id = c.id), 0) as rules_count,
               COALESCE((SELECT COUNT(*) FROM pseudo_code_test_case t WHERE t.challenge_id = c.id), 0) as test_cases_count,
               COALESCE((SELECT COUNT(*) FROM pseudo_code_attempt a WHERE a.challenge_id = c.id), 0) as attempt_count
        FROM pseudo_code_challenge c
        WHERE c.trainer_email = :email
        ORDER BY c.created_at DESC
    """, nativeQuery = true)
    List<Object[]> findTrainerChallengeListItems(String email);

    @Query("SELECT r.challenge.id, COUNT(r) FROM PseudoCodeRule r WHERE r.challenge.id IN :challengeIds GROUP BY r.challenge.id")
    List<Object[]> countRulesForChallenges(@org.springframework.data.repository.query.Param("challengeIds") List<Long> challengeIds);

    @Query("SELECT t.challenge.id, COUNT(t) FROM PseudoCodeTestCase t WHERE t.challenge.id IN :challengeIds GROUP BY t.challenge.id")
    List<Object[]> countTestCasesForChallenges(@org.springframework.data.repository.query.Param("challengeIds") List<Long> challengeIds);

    List<PseudoCodeChallenge> findByBatchIdInAndActiveTrueOrderByCreatedAtDesc(List<Long> batchIds);

    @Query("""
        SELECT DISTINCT c
        FROM PseudoCodeChallenge c
        LEFT JOIN FETCH c.testCases
        WHERE c.active = true
        AND (
            c.publicVisible = true
            OR c.batchId IS NULL
            OR c.batchId = 0
            OR (:hasBatches = true AND c.batchId IN :batchIds)
        )
        ORDER BY c.createdAt DESC, c.id DESC
    """)
    List<PseudoCodeChallenge> findActiveChallengesForStudentAndPublic(
            boolean hasBatches,
            List<Long> batchIds
    );

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

    @Query("""
        SELECT c
        FROM PseudoCodeChallenge c
        WHERE c.active = true AND c.publicVisible = true
        ORDER BY c.publishedAt DESC NULLS LAST, c.id DESC
    """)
    List<PseudoCodeChallenge> findActivePublicChallengesLight();

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

    List<PseudoCodeChallenge> findByChallengeGroupId(String challengeGroupId);
}