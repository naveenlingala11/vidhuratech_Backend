package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorAnswerReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MentorAnswerReactionRepository extends JpaRepository<MentorAnswerReaction, Long> {
    Optional<MentorAnswerReaction> findByAnswerIdAndUserIdAndEmoji(Long answerId, Long userId, String emoji);
    List<MentorAnswerReaction> findAllByAnswerId(Long answerId);
    List<MentorAnswerReaction> findAllByAnswerIdIn(List<Long> answerIds);
    long countByAnswerIdAndEmoji(Long answerId, String emoji);
}
