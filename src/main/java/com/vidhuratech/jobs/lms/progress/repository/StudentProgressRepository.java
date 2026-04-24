package com.vidhuratech.jobs.lms.progress.repository;

import com.vidhuratech.jobs.lms.progress.entity.StudentProgress;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface StudentProgressRepository
        extends JpaRepository<StudentProgress, Long> {

    Optional<StudentProgress> findByStudentEmailAndBatchIdAndSessionId(
            String email, Long batchId, Long sessionId
    );

    Long countByStudentEmailAndBatchIdAndCompletedTrue(
            String email, Long batchId
    );

    @Query("""
        SELECT sp FROM StudentProgress sp
        WHERE sp.studentEmail = :email
        AND sp.batchId = :batchId
        AND sp.lastWatched = true
    """)
    Optional<StudentProgress> findLastWatched(
            @Param("email") String email,
            @Param("batchId") Long batchId
    );

    @Modifying
    @Query("""
        UPDATE StudentProgress sp
        SET sp.lastWatched = false
        WHERE sp.studentEmail = :email
        AND sp.batchId = :batchId
    """)
    void clearLastWatched(
            @Param("email") String email,
            @Param("batchId") Long batchId
    );
}