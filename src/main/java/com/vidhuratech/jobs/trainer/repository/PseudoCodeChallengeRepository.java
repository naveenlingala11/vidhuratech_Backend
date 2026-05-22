package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.PseudoCodeChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PseudoCodeChallengeRepository extends JpaRepository<PseudoCodeChallenge, Long> {

    List<PseudoCodeChallenge> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);

    List<PseudoCodeChallenge> findByBatchIdInAndActiveTrueOrderByCreatedAtDesc(List<Long> batchIds);
}