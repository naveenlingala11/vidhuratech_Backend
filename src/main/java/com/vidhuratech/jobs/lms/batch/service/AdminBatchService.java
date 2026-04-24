package com.vidhuratech.jobs.lms.batch.service;

import com.vidhuratech.jobs.lms.batch.dto.BatchRequestDTO;

import java.util.Map;

public interface AdminBatchService {

    Map<String, Object> getAllBatches(
            String keyword,
            String status,
            Long courseId,
            Long trainerId,
            int page,
            int size
    );

    Object createBatch(BatchRequestDTO dto);

    Object updateBatch(Long id, BatchRequestDTO dto);

    void deleteBatch(Long id);
}