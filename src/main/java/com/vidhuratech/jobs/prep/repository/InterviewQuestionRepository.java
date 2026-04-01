package com.vidhuratech.jobs.prep.repository;

import com.vidhuratech.jobs.prep.entity.InterviewQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    Page<InterviewQuestion> findByCompanyAndRole(String company, String role, Pageable pageable);

    @Query("""
        SELECT q FROM InterviewQuestion q 
        WHERE q.company = :company 
        AND q.role = :role 
        AND (:search IS NULL OR q.question ILIKE CONCAT('%', :search, '%'))
    """)
    Page<InterviewQuestion> searchQuestions(
            String company,
            String role,
            String search,
            Pageable pageable
    );

    @Query("""
SELECT q FROM InterviewQuestion q
WHERE q.company = :company
AND q.role = :role
AND (:search IS NULL OR q.question LIKE %:search%)
AND (:type IS NULL OR q.type = :type)
AND (:difficulty IS NULL OR q.difficulty = :difficulty)
AND (:topic IS NULL OR q.topic = :topic)
""")
    Page<InterviewQuestion> filterQuestions(
            String company,
            String role,
            String search,
            String type,
            String difficulty,
            String topic,
            Pageable pageable
    );
}