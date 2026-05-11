package com.vidhuratech.jobs.trainer.service;

import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.entity.BatchEnrollment;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;
import com.vidhuratech.jobs.trainer.entity.*;
import com.vidhuratech.jobs.trainer.repository.MockInterviewRequestRepository;
import com.vidhuratech.jobs.trainer.repository.TrainingSubmissionRepository;
import com.vidhuratech.jobs.trainer.repository.TrainingWorkItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrainerWorkflowService {

    private final SecurityUtils securityUtils;
    private final BatchRepository batchRepository;
    private final BatchEnrollmentRepository enrollmentRepository;
    private final TrainingWorkItemRepository workItemRepository;
    private final TrainingSubmissionRepository submissionRepository;
    private final MockInterviewRequestRepository mockRepository;

    public List<Map<String, Object>> getStudents() {
        String email = securityUtils.getCurrentUserEmail();
        List<Batch> batches = batchRepository.findByTrainerEmail(email);

        return batches.stream()
                .flatMap(batch -> enrollmentRepository.findByBatchId(batch.getId()).stream())
                .filter(enrollment -> Boolean.TRUE.equals(enrollment.getActive()))
                .map(this::mapStudent)
                .toList();
    }

    public List<Map<String, Object>> getMockInterviewRequests() {
        String email = securityUtils.getCurrentUserEmail();
        return mockRepository.findByTrainerEmailOrderByCreatedAtDesc(email).stream()
                .map(this::mapMock)
                .toList();
    }

    public Map<String, Object> updateMockInterview(Long id, Map<String, Object> payload) {
        String email = securityUtils.getCurrentUserEmail();
        MockInterviewRequest request = mockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mock interview request not found"));

        if (request.getTrainer() == null || !email.equals(request.getTrainer().getEmail())) {
            throw new RuntimeException("Access denied");
        }

        if (payload.get("status") != null) {
            request.setStatus(MockInterviewStatus.valueOf(payload.get("status").toString()));
        }
        if (payload.get("meetingLink") != null) {
            request.setMeetingLink(payload.get("meetingLink").toString());
        }
        if (payload.get("trainerRemarks") != null) {
            request.setTrainerRemarks(payload.get("trainerRemarks").toString());
        }

        request.setUpdatedAt(LocalDateTime.now());
        return mapMock(mockRepository.save(request));
    }

    public Map<String, Object> createWorkItem(Map<String, Object> payload) {
        String email = securityUtils.getCurrentUserEmail();
        Long batchId = Long.valueOf(payload.get("batchId").toString());

        Batch batch = batchRepository.findByIdAndTrainerEmail(batchId, email)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        TrainingWorkItem item = TrainingWorkItem.builder()
                .batch(batch)
                .trainer(batch.getTrainer())
                .type(TrainingWorkType.valueOf(payload.getOrDefault("type", "ASSIGNMENT").toString()))
                .title(payload.getOrDefault("title", "").toString())
                .description(payload.getOrDefault("description", "").toString())
                .dueAt(LocalDateTime.parse(payload.get("dueAt").toString()))
                .totalMarks(Integer.valueOf(payload.getOrDefault("totalMarks", "100").toString()))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        return mapWorkItem(workItemRepository.save(item));
    }

    public List<Map<String, Object>> getWorkItems() {
        String email = securityUtils.getCurrentUserEmail();
        return workItemRepository.findByTrainerEmail(email).stream()
                .map(this::mapWorkItem)
                .toList();
    }

    public List<Map<String, Object>> getSubmissions() {
        String email = securityUtils.getCurrentUserEmail();
        return submissionRepository.findByTrainerEmail(email).stream()
                .map(this::mapSubmission)
                .toList();
    }

    public Map<String, Object> reviewSubmission(Long id, Map<String, Object> payload) {
        String email = securityUtils.getCurrentUserEmail();
        TrainingSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        if (!email.equals(submission.getWorkItem().getTrainer().getEmail())) {
            throw new RuntimeException("Access denied");
        }

        submission.setMarks(Integer.valueOf(payload.getOrDefault("marks", "0").toString()));
        submission.setFeedback(payload.getOrDefault("feedback", "").toString());
        submission.setStatus(TrainingSubmissionStatus.REVIEWED);
        submission.setReviewedAt(LocalDateTime.now());

        return mapSubmission(submissionRepository.save(submission));
    }

    private Map<String, Object> mapStudent(BatchEnrollment enrollment) {
        return Map.of(
                "id", enrollment.getStudent().getId(),
                "name", enrollment.getStudent().getName(),
                "email", enrollment.getStudent().getEmail(),
                "phone", enrollment.getStudent().getPhone(),
                "batchId", enrollment.getBatch().getId(),
                "batch", enrollment.getBatch().getName(),
                "course", enrollment.getBatch().getCourse().getTitle(),
                "status", Boolean.TRUE.equals(enrollment.getActive()) ? "Active" : "Inactive"
        );
    }

    private Map<String, Object> mapMock(MockInterviewRequest request) {

        Map<String, Object> map = new HashMap<>();

        map.put("id", request.getId());
        map.put("student", request.getStudent().getName());
        map.put("email", request.getStudent().getEmail());
        map.put("batch", request.getBatch().getName());
        map.put("topic", request.getTopic());
        map.put("preferredDate", request.getPreferredDate());
        map.put("preferredTime", request.getPreferredTime());
        map.put("notes", request.getNotes() == null ? "" : request.getNotes());
        map.put("status", request.getStatus() == null ? MockInterviewStatus.REQUESTED : request.getStatus());
        map.put("meetingLink", request.getMeetingLink() == null ? "" : request.getMeetingLink());
        map.put("trainerRemarks", request.getTrainerRemarks() == null ? "" : request.getTrainerRemarks());

        return map;
    }
    private Map<String, Object> mapWorkItem(TrainingWorkItem item) {
        return Map.of(
                "id", item.getId(),
                "batchId", item.getBatch().getId(),
                "batch", item.getBatch().getName(),
                "type", item.getType(),
                "title", item.getTitle(),
                "description", item.getDescription(),
                "dueAt", item.getDueAt(),
                "totalMarks", item.getTotalMarks()
        );
    }

    private Map<String, Object> mapSubmission(TrainingSubmission submission) {

        Map<String, Object> map = new HashMap<>();

        map.put("id", submission.getId());
        map.put("student", submission.getStudent().getName());
        map.put("email", submission.getStudent().getEmail());
        map.put("title", submission.getWorkItem().getTitle());
        map.put("type", submission.getWorkItem().getType());
        map.put("batch", submission.getWorkItem().getBatch().getName());
        map.put("answerText", submission.getAnswerText());
        map.put("marks", submission.getMarks() == null ? 0 : submission.getMarks());
        map.put("feedback", submission.getFeedback() == null ? "" : submission.getFeedback());
        map.put("status", submission.getStatus());
        map.put("submittedAt", submission.getSubmittedAt());

        return map;
    }
}
