package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorTagFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MentorTagFollowRepository extends JpaRepository<MentorTagFollow, Long> {
    Optional<MentorTagFollow> findByUserIdAndTagName(Long userId, String tagName);
    boolean existsByUserIdAndTagName(Long userId, String tagName);
    List<MentorTagFollow> findAllByUserId(Long userId);
}
