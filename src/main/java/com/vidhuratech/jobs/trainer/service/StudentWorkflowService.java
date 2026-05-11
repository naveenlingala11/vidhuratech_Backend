package com.vidhuratech.jobs.trainer.service;

import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.entity.BatchEnrollment;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;
import com.vidhuratech.jobs.trainer.entity.MockInterviewRequest;
import com.vidhuratech.jobs.trainer.entity.MockInterviewStatus;
import com.vidhuratech.jobs.trainer.entity.TrainingSubmission;
import com.vidhuratech.jobs.trainer.entity.TrainingWorkItem;
import com.vidhuratech.jobs.trainer.repository.MockInterviewRequestRepository;
import com.vidhuratech.jobs.trainer.repository.TrainingSubmissionRepository;
import com.vidhuratech.jobs.trainer.repository.TrainingWorkItemRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentWorkflowService {

    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final BatchRepository batchRepository;
    private final BatchEnrollmentRepository enrollmentRepository;
    private final TrainingWorkItemRepository workItemRepository;
    private final TrainingSubmissionRepository submissionRepository;
    private final MockInterviewRequestRepository mockRepository;

    public Map<String, Object> createMockInterviewRequest(Map<String, Object> payload) {
        String email = securityUtils.getCurrentUserEmail();
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Long batchId = Long.valueOf(payload.get("batchId").toString());
        BatchEnrollment enrollment = enrollmentRepository.findActiveByStudentEmail(email).stream()
                .filter(item -> item.getBatch().getId().equals(batchId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("You are not enrolled in this batch"));

        Batch batch = enrollment.getBatch();

        MockInterviewRequest request = MockInterviewRequest.builder()
                .student(student)
                .batch(batch)
                .trainer(batch.getTrainer())
                .topic(payload.getOrDefault("topic", "Mock Interview").toString())
                .preferredDate(LocalDate.parse(payload.get("preferredDate").toString()))
                .preferredTime(LocalTime.parse(payload.get("preferredTime").toString()))
                .notes(payload.getOrDefault("notes", "").toString())
                .status(MockInterviewStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

        return mapMock(mockRepository.save(request));
    }

    public List<Map<String, Object>> getMyMockInterviews() {
        String email = securityUtils.getCurrentUserEmail();
        return mockRepository.findByStudentEmailOrderByCreatedAtDesc(email).stream()
                .map(this::mapMock)
                .toList();
    }

    public List<Map<String, Object>> getMyWorkItems() {
        String email = securityUtils.getCurrentUserEmail();
        List<Long> batchIds = enrollmentRepository.findActiveByStudentEmail(email).stream()
                .map(enrollment -> enrollment.getBatch().getId())
                .toList();

        if (batchIds.isEmpty()) {
            return List.of();
        }

        return workItemRepository.findForStudentBatches(batchIds).stream()
                .map(item -> mapWorkItem(item, email))
                .toList();
    }

    public Map<String, Object> submitWork(Long workItemId, Map<String, Object> payload) {
        String email = securityUtils.getCurrentUserEmail();
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        TrainingWorkItem item = workItemRepository.findById(workItemId)
                .orElseThrow(() -> new RuntimeException("Work item not found"));

        boolean enrolled = enrollmentRepository.findActiveByStudentEmail(email).stream()
                .anyMatch(enrollment -> enrollment.getBatch().getId().equals(item.getBatch().getId()));

        if (!enrolled) {
            throw new RuntimeException("Access denied");
        }

        TrainingSubmission submission = submissionRepository.findByWorkItemIdAndStudentEmail(workItemId, email)
                .orElse(TrainingSubmission.builder()
                        .workItem(item)
                        .student(student)
                        .submittedAt(LocalDateTime.now())
                        .build());

        submission.setAnswerText(payload.getOrDefault("answerText", "").toString());
        submission.setSubmittedAt(LocalDateTime.now());

        return mapSubmission(submissionRepository.save(submission));
    }

    private Map<String, Object> mapWorkItem(TrainingWorkItem item, String email) {
        TrainingSubmission submission = submissionRepository.findByWorkItemIdAndStudentEmail(item.getId(), email)
                .orElse(null);

        return Map.of(
                "id", item.getId(),
                "batch", item.getBatch().getName(),
                "type", item.getType(),
                "title", item.getTitle(),
                "description", item.getDescription(),
                "dueAt", item.getDueAt(),
                "totalMarks", item.getTotalMarks(),
                "submitted", submission != null,
                "marks", submission == null || submission.getMarks() == null ? 0 : submission.getMarks(),
                "feedback", submission == null || submission.getFeedback() == null ? "" : submission.getFeedback()
        );
    }

    private Map<String, Object> mapMock(MockInterviewRequest request) {
        return Map.of(
                "id", request.getId(),
                "batch", request.getBatch() == null ? "" : request.getBatch().getName(),
                "topic", request.getTopic() == null ? "" : request.getTopic(),
                "preferredDate", request.getPreferredDate() == null ? "" : request.getPreferredDate(),
                "preferredTime", request.getPreferredTime() == null ? "" : request.getPreferredTime(),
                "status", request.getStatus() == null ? MockInterviewStatus.REQUESTED : request.getStatus(),
                "meetingLink", request.getMeetingLink() == null ? "" : request.getMeetingLink(),
                "trainerRemarks", request.getTrainerRemarks() == null ? "" : request.getTrainerRemarks()
        );
    }

    private Map<String, Object> mapSubmission(TrainingSubmission submission) {
        return Map.of(
                "id", submission.getId(),
                "title", submission.getWorkItem().getTitle(),
                "status", submission.getStatus(),
                "submittedAt", submission.getSubmittedAt()
        );
    }
}
