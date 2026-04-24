package com.vidhuratech.jobs.lms.batch.service;

import com.vidhuratech.jobs.lms.batch.dto.*;
import com.vidhuratech.jobs.lms.batch.entity.Batch;

import java.util.List;

public interface BatchService {

    Object getBatchById(Long id);

    List<BatchSessionResponseDTO> getSessions(Long batchId);

    BatchSessionResponseDTO createSession(Long batchId, BatchSessionRequestDTO dto);

    void publishSession(Long batchId, Long sessionId);

    void unpublishSession(Long batchId, Long sessionId);

    void deleteSession(Long batchId, Long sessionId);

    void enrollStudent(Long batchId, Long studentId);

    void removeEnrollment(Long enrollmentId);

    List<?> getEnrollments(Long batchId);

    Batch getActiveBatchByCourse(Long courseId);
}