package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorReviewRepository extends JpaRepository<MentorReview, Long> {

    @Query("SELECT r FROM MentorReview r JOIN FETCH r.student WHERE r.mentor.id = :mentorId AND r.status = 'PUBLISHED' ORDER BY r.createdAt DESC")
    List<MentorReview> findPublishedByMentorId(@Param("mentorId") Long mentorId);

    @Query("SELECT AVG(r.rating) FROM MentorReview r WHERE r.mentor.id = :mentorId AND r.status = 'PUBLISHED'")
    Double getAverageRatingByMentorId(@Param("mentorId") Long mentorId);

    long countByMentorIdAndStatus(Long mentorId, String status);

    Optional<MentorReview> findByMentorIdAndStudentId(Long mentorId, Long studentId);

    boolean existsByMentorIdAndStudentId(Long mentorId, Long studentId);
}
