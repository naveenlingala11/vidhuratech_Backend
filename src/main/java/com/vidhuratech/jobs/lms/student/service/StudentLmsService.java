package com.vidhuratech.jobs.lms.student.service;

import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.lms.batch.repository.BatchSessionRepository;
import com.vidhuratech.jobs.trainer.entity.Curriculum;
import com.vidhuratech.jobs.trainer.repository.CurriculumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentLmsService {

    private final BatchEnrollmentRepository enrollmentRepository;
    private final BatchSessionRepository sessionRepository;
    private final CurriculumRepository curriculumRepository;

    public List<?> getMyBatches() {
        String email = SecurityUtils.getCurrentUserEmail();

        return enrollmentRepository.findBatchesByStudentEmail(email)
                .stream()
                .map(batch -> Map.of(
                        "id", batch.getId(),
                        "name", batch.getName()
                ))
                .toList();
    }

    public List<?> getBatchSessions(Long batchId) {
        return sessionRepository
                .findByBatchIdAndPublishedTrueOrderBySessionDateAsc(batchId)
                .stream()
                .map(s -> Map.of(
                        "id", s.getId(),
                        "title", s.getTitle(),
                        "videoUrl", s.getVideoUrl(),
                        "duration", s.getDurationMinutes()
                ))
                .toList();
    }

    public Object getCurriculum(Long batchId) {

        String email = SecurityUtils.getCurrentUserEmail();

        // ✅ CHECK student enrolled
        boolean enrolled = enrollmentRepository
                .findBatchesByStudentEmail(email)
                .stream()
                .anyMatch(b -> b.getId().equals(batchId));

        if (!enrolled) {
            throw new RuntimeException("Access denied");
        }

        return curriculumRepository.findByBatchId(batchId)
                .map(Curriculum::getJsonData)
                .orElse(null);
    }
}