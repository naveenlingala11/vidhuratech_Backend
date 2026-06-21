package com.vidhuratech.jobs.mentor.repository;

import com.vidhuratech.jobs.mentor.entity.MentorStudentRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorStudentRelationRepository extends JpaRepository<MentorStudentRelation, Long> {

    @Query("SELECT r FROM MentorStudentRelation r JOIN FETCH r.student WHERE r.mentor.id = :mentorId")
    List<MentorStudentRelation> findAllByMentorIdWithStudent(@Param("mentorId") Long mentorId);

    @Query("SELECT r FROM MentorStudentRelation r JOIN FETCH r.student WHERE r.mentor.id = :mentorId AND r.student.name = :studentName")
    Optional<MentorStudentRelation> findByMentorIdAndStudentName(@Param("mentorId") Long mentorId, @Param("studentName") String studentName);

    long countByMentorIdAndStatus(Long mentorId, String status);

    @Query("SELECT r FROM MentorStudentRelation r JOIN FETCH r.mentor WHERE r.student.id = :studentId")
    List<MentorStudentRelation> findAllByStudentIdWithMentor(@Param("studentId") Long studentId);

    long countByStudentIdAndStatus(Long studentId, String status);

    boolean existsByMentorIdAndStudentId(Long mentorId, Long studentId);

    @Query("SELECT r FROM MentorStudentRelation r JOIN FETCH r.mentor JOIN FETCH r.student WHERE r.id = :id")
    Optional<MentorStudentRelation> findByIdWithMentorAndStudent(@Param("id") Long id);
}

