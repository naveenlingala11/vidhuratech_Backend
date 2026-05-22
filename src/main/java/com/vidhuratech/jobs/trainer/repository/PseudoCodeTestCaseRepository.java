package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.PseudoCodeTestCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PseudoCodeTestCaseRepository extends JpaRepository<PseudoCodeTestCase, Long> {
}