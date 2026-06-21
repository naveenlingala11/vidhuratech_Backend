package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorQuestionRepository extends JpaRepository<MentorQuestion, Long> {

    @Query("SELECT q FROM MentorQuestion q JOIN FETCH q.author ORDER BY q.createdAt DESC")
    List<MentorQuestion> findAllWithAuthor();

    @Query("SELECT q FROM MentorQuestion q JOIN FETCH q.author WHERE " +
           "LOWER(q.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(q.content) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(q.tags) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY q.createdAt DESC")
    List<MentorQuestion> searchQuestions(@Param("query") String query);

    List<MentorQuestion> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
