package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorQuestionLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MentorQuestionLikeRepository extends JpaRepository<MentorQuestionLike, Long> {

    boolean existsByQuestionIdAndUserId(Long questionId, Long userId);

    Optional<MentorQuestionLike> findByQuestionIdAndUserId(Long questionId, Long userId);

    long countByQuestionId(Long questionId);
}
