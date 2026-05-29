package com.vidhuratech.jobs.trainer.service;

import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
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
import org.springframework.transaction.annotation.Transactional;

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
    private final ActivityNotificationService notificationService;


    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudents() {
        String email = securityUtils.getCurrentUserEmail();

        return enrollmentRepository.findActiveStudentsByTrainerEmail(email)
                .stream()
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

        String statusValue = payload.getOrDefault("status", request.getStatus()).toString();

        MockInterviewStatus status;
        try {
            status = MockInterviewStatus.valueOf(statusValue);
        } catch (Exception e) {
            throw new RuntimeException("Invalid mock interview status");
        }

        String meetingLink = payload.getOrDefault("meetingLink", "").toString().trim();
        String remarks = payload.getOrDefault("trainerRemarks", "").toString().trim();

        if (status == MockInterviewStatus.SCHEDULED && meetingLink.isBlank()) {
            throw new RuntimeException("Meeting link is required to schedule interview");
        }

        request.setStatus(status);
        request.setMeetingLink(meetingLink);
        request.setTrainerRemarks(remarks);
        request.setUpdatedAt(LocalDateTime.now());

        MockInterviewRequest saved = mockRepository.save(request);

        notificationService.notifyStudent(
                saved.getStudent(),
                "Mock interview " + saved.getStatus(),
                "Your mock interview status changed to " + saved.getStatus(),
                "MOCK_INTERVIEW_UPDATED",
                "/dashboard/student/mock-interviews"
        );

        return mapMock(saved);
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

        TrainingWorkItem saved = workItemRepository.save(item);

        notificationService.notifyBatchStudents(
                batch.getEnrollments(),
                "New " + saved.getType(),
                saved.getTitle() + " assigned for " + batch.getName(),
                "WORK_ITEM_CREATED",
                "/dashboard/student/assignments"
        );

        notificationService.notifyAdmins(
                "Trainer assigned work",
                saved.getTitle() + " assigned for batch " + batch.getName(),
                "WORK_ITEM_CREATED",
                "/dashboard/admin/batches"
        );

        return mapWorkItem(saved);
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

        TrainingSubmission saved = submissionRepository.save(submission);

        notificationService.notifyStudent(
                saved.getStudent(),
                "Submission reviewed",
                "Your submission was reviewed: " + saved.getWorkItem().getTitle(),
                "SUBMISSION_REVIEWED",
                "/dashboard/student/assignments"
        );

        return mapSubmission(saved);
    }

    private Map<String, Object> mapStudent(BatchEnrollment enrollment) {
        Map<String, Object> map = new HashMap<>();

        var student = enrollment.getStudent();
        var batch = enrollment.getBatch();
        var course = batch != null ? batch.getCourse() : null;

        map.put("id", student != null ? student.getId() : null);
        map.put("name", student != null && student.getName() != null ? student.getName() : "Student");
        map.put("email", student != null && student.getEmail() != null ? student.getEmail() : "");
        map.put("phone", student != null && student.getPhone() != null ? student.getPhone() : "");
        map.put("batchId", batch != null ? batch.getId() : null);
        map.put("batch", batch != null && batch.getName() != null ? batch.getName() : "Batch");
        map.put("course", course != null && course.getTitle() != null ? course.getTitle() : "Course");
        map.put("status", Boolean.TRUE.equals(enrollment.getActive()) ? "Active" : "Inactive");

        return map;
    }

    private Map<String, Object> mapMock(MockInterviewRequest request) {
        Map<String, Object> map = new HashMap<>();

        map.put("id", request.getId());
        map.put("student", request.getStudent() == null ? "Student" : request.getStudent().getName());
        map.put("email", request.getStudent() == null ? "" : request.getStudent().getEmail());
        map.put("batch", request.getBatch() == null ? "Batch" : request.getBatch().getName());
        map.put("batchId", request.getBatch() == null ? null : request.getBatch().getId());
        map.put("topic", request.getTopic() == null ? "Mock Interview" : request.getTopic());
        map.put("preferredDate", request.getPreferredDate() == null ? "" : request.getPreferredDate());
        map.put("preferredTime", request.getPreferredTime() == null ? "" : request.getPreferredTime());
        map.put("notes", request.getNotes() == null ? "" : request.getNotes());
        map.put("status", request.getStatus() == null ? MockInterviewStatus.REQUESTED : request.getStatus());
        map.put("meetingLink", request.getMeetingLink() == null ? "" : request.getMeetingLink());
        map.put("trainerRemarks", request.getTrainerRemarks() == null ? "" : request.getTrainerRemarks());
        map.put("createdAt", request.getCreatedAt());
        map.put("updatedAt", request.getUpdatedAt());

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
