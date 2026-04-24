package com.vidhuratech.jobs.lms.batch.repository;

import com.vidhuratech.jobs.lms.batch.entity.BatchSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchSessionRepository extends JpaRepository<BatchSession, Long> {

    List<BatchSession> findByBatchIdOrderBySessionDateDesc(Long batchId);

    List<BatchSession> findByBatchIdAndPublishedTrueOrderBySessionDateAsc(Long batchId);
}