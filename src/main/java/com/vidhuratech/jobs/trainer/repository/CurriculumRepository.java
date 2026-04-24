package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.Curriculum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {

    Optional<Curriculum> findByBatchId(Long batchId);
}