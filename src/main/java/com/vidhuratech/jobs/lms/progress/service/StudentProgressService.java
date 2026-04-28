package com.vidhuratech.jobs.lms.progress.service;

import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.batch.repository.BatchSessionRepository;
import com.vidhuratech.jobs.lms.progress.entity.StudentProgress;
import com.vidhuratech.jobs.lms.progress.repository.StudentProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentProgressService {

    private final StudentProgressRepository repo;
    private final BatchSessionRepository sessionRepository;
    private final SecurityUtils securityUtils;

    /* =========================
       MARK COMPLETED
    ========================= */
    @Transactional
    public void markCompleted(Long batchId, Long sessionId) {

        String email = securityUtils.getCurrentUserEmail();

        StudentProgress p = repo
                .findByStudentEmailAndBatchIdAndSessionId(email, batchId, sessionId)
                .orElse(StudentProgress.builder()
                        .studentEmail(email)
                        .batchId(batchId)
                        .sessionId(sessionId)
                        .build());

        p.setCompleted(true);

        repo.save(p);
    }

    /* =========================
       UPDATE LAST WATCHED
    ========================= */
    @Transactional
    public void updateLastWatched(Long batchId, Long sessionId) {

        String email = securityUtils.getCurrentUserEmail();

        repo.clearLastWatched(email, batchId);

        StudentProgress p = repo
                .findByStudentEmailAndBatchIdAndSessionId(email, batchId, sessionId)
                .orElse(StudentProgress.builder()
                        .studentEmail(email)
                        .batchId(batchId)
                        .sessionId(sessionId)
                        .build());

        p.setLastWatched(true);

        repo.save(p);
    }

    /* =========================
       UPDATE WITH TIME
    ========================= */
    @Transactional
    public void updateResumeWithTime(Long batchId, Long sessionId, Long time) {

        String email = securityUtils.getCurrentUserEmail();

        repo.clearLastWatched(email, batchId);

        StudentProgress p = repo
                .findByStudentEmailAndBatchIdAndSessionId(email, batchId, sessionId)
                .orElse(StudentProgress.builder()
                        .studentEmail(email)
                        .batchId(batchId)
                        .sessionId(sessionId)
                        .build());

        p.setLastWatched(true);
        p.setLastWatchedTime(time);

        repo.save(p);
    }

    /* =========================
       GET PROGRESS %
    ========================= */
    public int getProgress(Long batchId) {

        String email = securityUtils.getCurrentUserEmail();

        long total = sessionRepository
                .findByBatchIdAndPublishedTrueOrderBySessionDateAsc(batchId)
                .size();

        long completed = repo
                .countByStudentEmailAndBatchIdAndCompletedTrue(email, batchId);

        if (total == 0) return 0;

        return (int)((completed * 100) / total);
    }

    /* =========================
       RESUME (SESSION + TIME)
    ========================= */
    public Map<String, Object> getResumeData(Long batchId) {

        String email = securityUtils.getCurrentUserEmail();

        return repo.findLastWatched(email, batchId)
                .map(p -> Map.<String, Object>of(
                        "sessionId", p.getSessionId(),
                        "time", p.getLastWatchedTime() == null ? 0 : p.getLastWatchedTime()
                ))
                .orElse(null);
    }
}