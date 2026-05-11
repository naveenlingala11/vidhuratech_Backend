package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.TrainingWorkItem;
import com.vidhuratech.jobs.trainer.entity.TrainingWorkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrainingWorkItemRepository extends JpaRepository<TrainingWorkItem, Long> {

    List<TrainingWorkItem> findByBatchIdAndActiveTrueOrderByDueAtAsc(Long batchId);

    @Query("""
        SELECT w
        FROM TrainingWorkItem w
        WHERE w.batch.trainer.email = :trainerEmail
        AND w.active = true
        ORDER BY w.dueAt ASC
    """)
    List<TrainingWorkItem> findByTrainerEmail(String trainerEmail);

    @Query("""
        SELECT w
        FROM TrainingWorkItem w
        WHERE w.batch.id IN :batchIds
        AND w.active = true
        ORDER BY w.dueAt ASC
    """)
    List<TrainingWorkItem> findForStudentBatches(List<Long> batchIds);

    @Query("""
        SELECT COUNT(w)
        FROM TrainingWorkItem w
        WHERE w.trainer.email = :trainerEmail
        AND w.type = :type
        AND w.active = true
    """)
    Long countByTrainerEmailAndType(String trainerEmail, TrainingWorkType type);
}
