package com.vidhuratech.jobs.trainer.service;

import com.vidhuratech.jobs.dashboard.dto.DashboardStatsResponse;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.trainer.entity.Curriculum;
import com.vidhuratech.jobs.trainer.repository.CurriculumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TrainerDashboardService {

    private final BatchRepository batchRepository;
    private final CurriculumRepository curriculumRepository;

    /* =========================
       DASHBOARD
    ========================= */
    public DashboardStatsResponse getDashboard() {

        String email = SecurityUtils.getCurrentUserEmail();

        long assignedBatches =
                batchRepository.countByTrainerEmail(email);

        List<Map<String, Object>> batches = getBatches();

        Map<String, Object> stats = new HashMap<>();
        stats.put("assignedBatches", assignedBatches);
        stats.put("totalStudents", 0);
        stats.put("pendingReviews", 0);
        stats.put("todaysSessions", 0); // ✅ FIXED
        stats.put("avgAttendance", 0);
        stats.put("assignmentsSubmitted", 0);

        Map<String, List<?>> sections = new HashMap<>();
        sections.put("batches", batches);
        sections.put("upcomingSessions", List.of());
        sections.put("studentActivities", List.of());

        return DashboardStatsResponse.builder()
                .stats(stats)
                .sections(sections)
                .build();
    }

    /* =========================
       BATCHES
    ========================= */
    public List<Map<String, Object>> getBatches() {

        String email = SecurityUtils.getCurrentUserEmail();

        return batchRepository.findByTrainerEmail(email)
                .stream()
                .map(batch -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", batch.getId());
                    map.put("name", batch.getName());
                    map.put("students", 0);
                    return map;
                })
                .toList();
    }

    /* =========================
       STUDENTS (placeholder)
    ========================= */
    public List<?> getStudents() {
        return List.of(); // TODO: integrate student module
    }

    /* =========================
       CURRICULUM SAVE (UPSERT)
    ========================= */
    public void saveOrUpdateCurriculum(Long batchId, String json) {

        String email = SecurityUtils.getCurrentUserEmail();

        // ✅ SECURITY CHECK
        batchRepository.findByIdAndTrainerEmail(batchId, email)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        Optional<Curriculum> existing =
                curriculumRepository.findByBatchId(batchId);

        Curriculum c;

        if (existing.isPresent()) {
            c = existing.get();
            c.setJsonData(json);
        } else {
            c = Curriculum.builder()
                    .batchId(batchId)
                    .trainerEmail(email)
                    .jsonData(json)
                    .build();
        }

        curriculumRepository.save(c);
    }

    /* =========================
       GET CURRICULUM
    ========================= */
    public Optional<Curriculum> getCurriculum(Long batchId) {

        String email = SecurityUtils.getCurrentUserEmail();

        // ✅ Only trainer who owns batch
        batchRepository.findByIdAndTrainerEmail(batchId, email)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        return curriculumRepository.findByBatchId(batchId);
    }
}