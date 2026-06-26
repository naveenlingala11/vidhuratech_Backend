package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    @Query("""
        SELECT DISTINCT a
        FROM Assessment a
        LEFT JOIN FETCH a.questions
        LEFT JOIN FETCH a.batch b
        LEFT JOIN FETCH b.course
        LEFT JOIN FETCH a.trainer t
        WHERE t.email = :email
        ORDER BY a.id DESC
    """)
    List<Assessment> findByTrainerEmail(String email);

    @Query("""
        SELECT DISTINCT a
        FROM Assessment a
        LEFT JOIN FETCH a.questions
        LEFT JOIN FETCH a.batch b
        LEFT JOIN FETCH b.course
        WHERE b.id IN :batchIds
        AND a.active = true
        AND (a.startTime IS NULL OR a.startTime <= :now)
        AND (a.endTime IS NULL OR a.endTime >= :now)
        ORDER BY a.id DESC
    """)
    List<Assessment> findActiveAssessmentsForStudent(List<Long> batchIds, LocalDateTime now);

    @Query("""
        SELECT DISTINCT a
        FROM Assessment a
        LEFT JOIN FETCH a.questions
        LEFT JOIN FETCH a.batch b
        LEFT JOIN FETCH b.course
        WHERE a.active = true
        AND (
            a.publicVisible = true
            OR b IS NULL
            OR (:hasBatches = true AND b.id IN :batchIds)
        )
        AND (a.startTime IS NULL OR a.startTime <= :now)
        AND (a.endTime IS NULL OR a.endTime >= :now)
        ORDER BY a.id DESC
    """)
    List<Assessment> findActiveAssessmentsForStudentAndPublic(
            boolean hasBatches,
            List<Long> batchIds,
            LocalDateTime now
    );

    @Query("""
        SELECT DISTINCT a
        FROM Assessment a
        LEFT JOIN FETCH a.questions
        LEFT JOIN FETCH a.batch b
        LEFT JOIN FETCH b.course
        LEFT JOIN FETCH a.trainer
        WHERE a.id = :id
    """)
    Optional<Assessment> findDetailedAssessment(Long id);

    @Query("""
    SELECT DISTINCT a
    FROM Assessment a
    LEFT JOIN FETCH a.questions
    LEFT JOIN FETCH a.batch b
    LEFT JOIN FETCH b.course
    WHERE a.active = true
    AND a.publicVisible = true
    AND (a.startTime IS NULL OR a.startTime <= :now)
    AND (a.endTime IS NULL OR a.endTime >= :now)
    ORDER BY a.publishedAt DESC, a.id DESC
""")
    List<Assessment> findActivePublicAssessments(LocalDateTime now);

    @Query("""
    SELECT a
    FROM Assessment a
    WHERE a.active = true
    AND a.publicVisible = true
    AND (a.startTime IS NULL OR a.startTime <= :now)
    AND (a.endTime IS NULL OR a.endTime >= :now)
    ORDER BY a.publishedAt DESC, a.id DESC
""")
    List<Assessment> findActivePublicAssessmentsLight(LocalDateTime now);

    @Query("SELECT q.assessment.id, COUNT(q) FROM AssessmentQuestion q WHERE q.assessment.id IN :assessmentIds GROUP BY q.assessment.id")
    List<Object[]> countQuestionsForAssessments(@org.springframework.data.repository.query.Param("assessmentIds") List<Long> assessmentIds);

    @Query("""
    SELECT DISTINCT a
    FROM Assessment a
    LEFT JOIN FETCH a.questions
    LEFT JOIN FETCH a.batch b
    LEFT JOIN FETCH b.course
    LEFT JOIN FETCH a.trainer t
    ORDER BY a.id DESC
""")
    List<Assessment> findAllPublicPracticeCandidates();
}