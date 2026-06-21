package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorQuestionFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MentorQuestionFollowRepository extends JpaRepository<MentorQuestionFollow, Long> {
    Optional<MentorQuestionFollow> findByQuestionIdAndUserId(Long questionId, Long userId);
    boolean existsByQuestionIdAndUserId(Long questionId, Long userId);
    List<MentorQuestionFollow> findAllByUserId(Long userId);
    List<MentorQuestionFollow> findAllByQuestionId(Long questionId);
}
