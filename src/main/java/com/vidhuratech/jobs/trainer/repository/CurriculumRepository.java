package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.Curriculum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {

    Optional<Curriculum> findByBatchId(Long batchId);

    Optional<Curriculum> findByCourseId(Long courseId);

    Optional<Curriculum> findByCourseIdAndPublished(Long courseId, Boolean published);

    java.util.List<Curriculum> findByTrainerEmail(String trainerEmail);

    java.util.List<Curriculum> findByCourseIdAndTrainerEmail(Long courseId, String trainerEmail);

    java.util.List<Curriculum> findByPublishedFalse();
}