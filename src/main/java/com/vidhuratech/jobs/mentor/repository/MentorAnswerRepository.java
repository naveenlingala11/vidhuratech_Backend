package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorAnswerRepository extends JpaRepository<MentorAnswer, Long> {

    @Query("SELECT a FROM MentorAnswer a JOIN FETCH a.author WHERE a.question.id = :questionId ORDER BY a.createdAt ASC")
    List<MentorAnswer> findAllByQuestionIdWithAuthor(@Param("questionId") Long questionId);

    List<MentorAnswer> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
