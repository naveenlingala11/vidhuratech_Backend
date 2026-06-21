package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorBookingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorBookingRequestRepository extends JpaRepository<MentorBookingRequest, Long> {

    @Query("SELECT b FROM MentorBookingRequest b JOIN FETCH b.student WHERE b.mentor.id = :mentorId ORDER BY b.createdAt DESC")
    List<MentorBookingRequest> findAllByMentorId(@Param("mentorId") Long mentorId);

    @Query("SELECT b FROM MentorBookingRequest b JOIN FETCH b.student WHERE b.mentor.id = :mentorId AND b.status = :status ORDER BY b.createdAt DESC")
    List<MentorBookingRequest> findByMentorIdAndStatus(@Param("mentorId") Long mentorId, @Param("status") String status);

    @Query("SELECT b FROM MentorBookingRequest b JOIN FETCH b.mentor WHERE b.student.id = :studentId ORDER BY b.createdAt DESC")
    List<MentorBookingRequest> findAllByStudentId(@Param("studentId") Long studentId);

    long countByMentorIdAndStatus(Long mentorId, String status);

    boolean existsByMentorIdAndStudentIdAndStatus(Long mentorId, Long studentId, String status);
}
