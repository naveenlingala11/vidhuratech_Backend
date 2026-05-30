package com.vidhuratech.jobs.prep.repository;

import com.vidhuratech.jobs.prep.entity.InterviewQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    @Query("""
        SELECT q FROM InterviewQuestion q
        WHERE q.trainer.email = :email
        ORDER BY q.createdAt DESC, q.id DESC
    """)
    List<InterviewQuestion> findByTrainerEmailOrderByCreatedAtDesc(@Param("email") String email);

    @Query("""
        SELECT q FROM InterviewQuestion q
        WHERE q.id = :id
        AND q.trainer.email = :email
    """)
    Optional<InterviewQuestion> findByIdAndTrainerEmail(
            @Param("id") Long id,
            @Param("email") String email
    );

    List<InterviewQuestion> findByBatchIdInAndActiveTrueOrderByCreatedAtDesc(List<Long> batchIds);

    @Query("""
        SELECT q FROM InterviewQuestion q
        WHERE q.active = true
        AND q.publicVisible = true
        AND (:company = '' OR LOWER(q.company) = :company)
        AND (:role = '' OR LOWER(q.role) = :role)
        AND (:search = '' OR LOWER(q.question) LIKE CONCAT('%', :search, '%'))
        AND (:type = '' OR q.type = :type)
        AND (:difficulty = '' OR q.difficulty = :difficulty)
        AND (:topic = '' OR LOWER(q.topic) = :topic)
        ORDER BY q.publishedAt DESC NULLS LAST, q.id DESC
    """)
    Page<InterviewQuestion> findPublicQuestions(
            @Param("company") String company,
            @Param("role") String role,
            @Param("search") String search,
            @Param("type") String type,
            @Param("difficulty") String difficulty,
            @Param("topic") String topic,
            Pageable pageable
    );

    @Query("""
        SELECT q FROM InterviewQuestion q
        WHERE q.batchId IN :batchIds
        AND q.active = true
        AND (:company = '' OR LOWER(q.company) = :company)
        AND (:role = '' OR LOWER(q.role) = :role)
        AND (:search = '' OR LOWER(q.question) LIKE CONCAT('%', :search, '%'))
        AND (:type = '' OR q.type = :type)
        AND (:difficulty = '' OR q.difficulty = :difficulty)
        AND (:topic = '' OR LOWER(q.topic) = :topic)
        ORDER BY q.createdAt DESC, q.id DESC
    """)
    Page<InterviewQuestion> findStudentQuestions(
            @Param("batchIds") List<Long> batchIds,
            @Param("company") String company,
            @Param("role") String role,
            @Param("search") String search,
            @Param("type") String type,
            @Param("difficulty") String difficulty,
            @Param("topic") String topic,
            Pageable pageable
    );

    @Query("""
        SELECT q FROM InterviewQuestion q
        LEFT JOIN FETCH q.trainer
        ORDER BY q.createdAt DESC, q.id DESC
    """)
    List<InterviewQuestion> findAllPublicCandidates();

    @Query("""
    SELECT q FROM InterviewQuestion q
    LEFT JOIN FETCH q.trainer
    WHERE q.id = :id
""")
    Optional<InterviewQuestion> findPublicPracticeCandidateById(@Param("id") Long id);
}