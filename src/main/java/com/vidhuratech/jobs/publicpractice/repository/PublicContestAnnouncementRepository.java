package com.vidhuratech.jobs.publicpractice.repository;

import com.vidhuratech.jobs.publicpractice.entity.PublicContestAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PublicContestAnnouncementRepository
        extends JpaRepository<PublicContestAnnouncement, Long> {

    List<PublicContestAnnouncement> findTop10ByPublishedTrueOrderByCreatedAtDesc();
}