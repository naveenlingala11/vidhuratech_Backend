package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorAnswerVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorAnswerVoteRepository extends JpaRepository<MentorAnswerVote, Long> {

    Optional<MentorAnswerVote> findByAnswerIdAndUserId(Long answerId, Long userId);

    @Query("SELECT COUNT(v) FROM MentorAnswerVote v WHERE v.answer.id = :answerId AND v.voteType = 'UP'")
    long countUpvotesByAnswerId(Long answerId);

    @Query("SELECT COUNT(v) FROM MentorAnswerVote v WHERE v.answer.id = :answerId AND v.voteType = 'DOWN'")
    long countDownvotesByAnswerId(Long answerId);

    List<MentorAnswerVote> findAllByAnswerIdIn(List<Long> answerIds);
}
