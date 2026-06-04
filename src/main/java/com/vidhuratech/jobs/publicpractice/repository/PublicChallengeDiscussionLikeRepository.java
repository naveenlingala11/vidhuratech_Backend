package com.vidhuratech.jobs.publicpractice.repository;

import com.vidhuratech.jobs.publicpractice.entity.PublicChallengeDiscussionLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublicChallengeDiscussionLikeRepository
        extends JpaRepository<PublicChallengeDiscussionLike, Long> {

    Optional<PublicChallengeDiscussionLike> findByDiscussionIdAndLikerKey(
            Long discussionId,
            String likerKey
    );

    long countByDiscussionId(Long discussionId);
}