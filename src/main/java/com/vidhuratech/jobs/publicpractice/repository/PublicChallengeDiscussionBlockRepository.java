package com.vidhuratech.jobs.publicpractice.repository;

import com.vidhuratech.jobs.publicpractice.entity.PublicChallengeDiscussionBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PublicChallengeDiscussionBlockRepository
        extends JpaRepository<PublicChallengeDiscussionBlock, Long> {

    List<PublicChallengeDiscussionBlock> findByBlockerKey(String blockerKey);

    Optional<PublicChallengeDiscussionBlock> findByBlockerKeyAndBlockedAuthorKey(
            String blockerKey,
            String blockedAuthorKey
    );

    boolean existsByBlockerKeyAndBlockedAuthorKey(String blockerKey, String blockedAuthorKey);
}