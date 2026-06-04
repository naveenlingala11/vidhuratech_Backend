package com.vidhuratech.jobs.publicpractice.repository;

import com.vidhuratech.jobs.publicpractice.entity.PublicChallengeDiscussion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicChallengeDiscussionRepository
        extends JpaRepository<PublicChallengeDiscussion, Long> {

    List<PublicChallengeDiscussion> findByChallengeIdAndParentIdIsNullOrderByCreatedAtDesc(Long challengeId);

    List<PublicChallengeDiscussion> findByParentIdOrderByCreatedAtAsc(Long parentId);
}