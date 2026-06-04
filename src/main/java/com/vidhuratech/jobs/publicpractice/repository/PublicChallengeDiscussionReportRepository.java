package com.vidhuratech.jobs.publicpractice.repository;

import com.vidhuratech.jobs.publicpractice.entity.PublicChallengeDiscussionReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublicChallengeDiscussionReportRepository
        extends JpaRepository<PublicChallengeDiscussionReport, Long> {

    Optional<PublicChallengeDiscussionReport> findByDiscussionIdAndReporterKey(
            Long discussionId,
            String reporterKey
    );

    long countByDiscussionId(Long discussionId);
}