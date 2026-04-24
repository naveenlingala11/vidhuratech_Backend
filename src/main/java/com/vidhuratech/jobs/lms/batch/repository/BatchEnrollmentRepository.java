package com.vidhuratech.jobs.lms.batch.repository;

import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.entity.BatchEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BatchEnrollmentRepository
        extends JpaRepository<BatchEnrollment, Long> {

    boolean existsByBatchIdAndStudentId(Long batchId, Long studentId);

    @Query("""
        SELECT COUNT(be)
        FROM BatchEnrollment be
        WHERE be.batch.trainer.email = :email
        AND be.active = true
    """)
    Long countStudentsByTrainerEmail(String email);

    @Query("""
    SELECT be.batch
    FROM BatchEnrollment be
    WHERE be.student.email = :email
    AND be.active = true
    """)
    List<Batch> findBatchesByStudentEmail(String email);

    List<BatchEnrollment> findByBatchId(Long batchId);

    Long countByStudentEmailAndActiveTrue(String email);

    @Query("""
    SELECT be
    FROM BatchEnrollment be
    WHERE be.student.email = :email
    AND be.active = true
""")
    List<BatchEnrollment> findActiveByStudentEmail(String email);

    @Query("""
    SELECT COUNT(be)
    FROM BatchEnrollment be
    WHERE be.batch.id = :batchId
    AND be.active = true
""")
    Long countByBatchId(Long batchId);
}