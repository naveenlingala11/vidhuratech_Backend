package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.TrainingContent;
import com.vidhuratech.jobs.trainer.entity.TrainingContentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingContentRepository extends JpaRepository<TrainingContent, Long> {

    List<TrainingContent> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);

    List<TrainingContent> findByBatchIdInOrderByCreatedAtDesc(List<Long> batchIds);

    long countByTrainerEmail(String trainerEmail);

    long countByTrainerEmailAndType(String trainerEmail, TrainingContentType type);
}