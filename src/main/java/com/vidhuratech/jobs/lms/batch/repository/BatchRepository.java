package com.vidhuratech.jobs.lms.batch.repository;

import com.vidhuratech.jobs.lms.batch.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long>, JpaSpecificationExecutor<Batch> {
   Optional<Batch> findByIdAndTrainerEmail(Long id, String email);

    Long countByTrainerEmail(String email);

    List<Batch> findByTrainerEmail(String email);

    Optional<Batch> findByName(String name);

    Optional<Batch> findTopByCourseIdOrderByStartDateDesc(Long courseId);

    @Query("""
    SELECT b FROM Batch b
    WHERE b.course.id = :courseId
    AND b.status = 'ACTIVE'
    AND b.active = true
    """)
    Optional<Batch> findActiveBatch(Long courseId);
}