package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorSessionRepository extends JpaRepository<MentorSession, Long> {

    @Query("SELECT s FROM MentorSession s JOIN FETCH s.student WHERE s.mentor.id = :mentorId ORDER BY s.id DESC")
    List<MentorSession> findAllByMentorIdWithStudent(@Param("mentorId") Long mentorId);

    long countByMentorIdAndStatus(Long mentorId, String status);

    @Query("SELECT s FROM MentorSession s JOIN FETCH s.mentor WHERE s.student.id = :studentId ORDER BY s.id DESC")
    List<MentorSession> findAllByStudentIdWithMentor(@Param("studentId") Long studentId);

    long countByStudentIdAndStatus(Long studentId, String status);
}
