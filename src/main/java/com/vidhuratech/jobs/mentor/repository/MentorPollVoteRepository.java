package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorPollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface MentorPollVoteRepository extends JpaRepository<MentorPollVote, Long> {
    Optional<MentorPollVote> findByPollIdAndUserId(Long pollId, Long userId);
    boolean existsByPollIdAndUserId(Long pollId, Long userId);
    List<MentorPollVote> findAllByPollId(Long pollId);
    List<MentorPollVote> findAllByPollQuestionIdAndUserId(Long questionId, Long userId);
}
