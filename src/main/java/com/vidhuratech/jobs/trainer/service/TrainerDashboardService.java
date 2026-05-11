package com.vidhuratech.jobs.trainer.service;

import com.vidhuratech.jobs.dashboard.dto.DashboardStatsResponse;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.trainer.entity.Curriculum;
import com.vidhuratech.jobs.trainer.entity.MockInterviewStatus;
import com.vidhuratech.jobs.trainer.entity.TrainingSubmissionStatus;
import com.vidhuratech.jobs.trainer.repository.CurriculumRepository;
import com.vidhuratech.jobs.trainer.repository.MockInterviewRequestRepository;
import com.vidhuratech.jobs.trainer.repository.TrainingSubmissionRepository;
import com.vidhuratech.jobs.trainer.repository.TrainingWorkItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TrainerDashboardService {

    private final BatchRepository batchRepository;
    private final BatchEnrollmentRepository enrollmentRepository;
    private final CurriculumRepository curriculumRepository;
    private final SecurityUtils securityUtils;
    private final ObjectMapper objectMapper;
    private final TrainerWorkflowService workflowService;
    private final TrainingWorkItemRepository workItemRepository;
    private final TrainingSubmissionRepository submissionRepository;
    private final MockInterviewRequestRepository mockRepository;

    public DashboardStatsResponse getDashboard() {
        String email = securityUtils.getCurrentUserEmail();

        long assignedBatches = batchRepository.countByTrainerEmail(email);
        long totalStudents = enrollmentRepository.countStudentsByTrainerEmail(email);
        long pendingReviews = submissionRepository.countByTrainerEmailAndStatus(email, TrainingSubmissionStatus.SUBMITTED);
        long mockRequests = mockRepository.countByTrainerEmailAndStatus(email, MockInterviewStatus.REQUESTED);

        List<Map<String, Object>> batches = getBatches();

        Map<String, Object> stats = new HashMap<>();
        stats.put("assignedBatches", assignedBatches);
        stats.put("totalStudents", totalStudents);
        stats.put("pendingReviews", pendingReviews);
        stats.put("todaysSessions", mockRequests);
        stats.put("avgAttendance", 0);
        stats.put("assignmentsSubmitted", submissionRepository.findByTrainerEmail(email).size());

        Map<String, List<?>> sections = new HashMap<>();
        sections.put("batches", batches);
        sections.put("upcomingSessions", workflowService.getMockInterviewRequests().stream().limit(5).toList());
        sections.put("studentActivities", workflowService.getSubmissions().stream().limit(5).toList());

        return DashboardStatsResponse.builder()
                .stats(stats)
                .sections(sections)
                .build();
    }

    public List<Map<String, Object>> getBatches() {
        String email = securityUtils.getCurrentUserEmail();

        return batchRepository.findByTrainerEmail(email)
                .stream()
                .map(batch -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", batch.getId());
                    map.put("name", batch.getName());
                    map.put("course", batch.getCourse().getTitle());
                    map.put("students", enrollmentRepository.countByBatchId(batch.getId()));
                    map.put("status", batch.getStatus());
                    map.put("zoomTime", batch.getZoomTime());
                    return map;
                })
                .toList();
    }

    public List<?> getStudents() {
        return workflowService.getStudents();
    }

    public void saveOrUpdateCurriculum(Long batchId, String json) {
        try {
            objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON format");
        }

        String email = securityUtils.getCurrentUserEmail();

        batchRepository.findByIdAndTrainerEmail(batchId, email)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        Optional<Curriculum> existing = curriculumRepository.findByBatchId(batchId);

        Curriculum curriculum = existing.orElseGet(() -> Curriculum.builder()
                .batchId(batchId)
                .trainerEmail(email)
                .build());

        curriculum.setJsonData(json);
        curriculumRepository.save(curriculum);
    }

    public Optional<Curriculum> getCurriculum(Long batchId) {
        String email = securityUtils.getCurrentUserEmail();

        batchRepository.findByIdAndTrainerEmail(batchId, email)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        return curriculumRepository.findByBatchId(batchId);
    }

    public String getCurriculumPreview(Long batchId) {
        return curriculumRepository.findByBatchId(batchId)
                .map(Curriculum::getJsonData)
                .orElse(null);
    }
}
