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
import com.vidhuratech.jobs.trainer.repository.MockInterviewJoinHistoryRepository;
import com.vidhuratech.jobs.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.vidhuratech.jobs.mentor.entity.MentorSession;
import com.vidhuratech.jobs.mentor.repository.MentorSessionRepository;

@Service
@RequiredArgsConstructor
public class TrainerWorkflowService {

    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final BatchRepository batchRepository;
    private final BatchEnrollmentRepository enrollmentRepository;
    private final TrainingWorkItemRepository workItemRepository;
    private final TrainingSubmissionRepository submissionRepository;
    private final MockInterviewRequestRepository mockRepository;
    private final ActivityNotificationService notificationService;
    private final MentorSessionRepository mentorSessionRepository;
    private final MockInterviewJoinHistoryRepository joinHistoryRepository;


    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudents() {
        String email = securityUtils.getCurrentUserEmail();

        return enrollmentRepository.findActiveStudentsByTrainerEmail(email)
                .stream()
                .map(this::mapStudent)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMockInterviewRequests() {
        String email = securityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();

        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.SUPER_ADMIN) {
            mockRepository.findAll().forEach(m -> list.add(mapMock(m)));
            mentorSessionRepository.findAll().forEach(s -> list.add(mapMentorSessionToMock(s)));
        } else if (user.getRole() == UserRole.MENTOR) {
            mockRepository.findByTrainerEmailOrderByCreatedAtDesc(email).forEach(m -> list.add(mapMock(m)));
            mentorSessionRepository.findAllByMentorIdWithStudent(user.getId()).forEach(s -> list.add(mapMentorSessionToMock(s)));
        } else {
            mockRepository.findByTrainerEmailOrderByCreatedAtDesc(email).forEach(m -> list.add(mapMock(m)));
        }

        list.sort((a, b) -> {
            try {
                LocalDateTime timeA = (LocalDateTime) a.get("createdAt");
                LocalDateTime timeB = (LocalDateTime) b.get("createdAt");
                if (timeA != null && timeB != null) {
                    return timeB.compareTo(timeA);
                }
            } catch (Exception e) {}
            return 0;
        });

        return list;
    }

    public Map<String, Object> updateMockInterview(Long id, Map<String, Object> payload) {
        if (id < 0) {
            String email = securityUtils.getCurrentUserEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            MentorSession session = mentorSessionRepository.findById(-id)
                    .orElseThrow(() -> new RuntimeException("Mentor session not found"));

            if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.SUPER_ADMIN) {
                if (session.getMentor() == null || !email.equals(session.getMentor().getEmail())) {
                    throw new RuntimeException("Access denied");
                }
            }

            if (payload.containsKey("status")) {
                session.setStatus(payload.get("status").toString());
            }
            if (payload.containsKey("meetingLink")) {
                session.setMeetingLink(payload.get("meetingLink") != null ? payload.get("meetingLink").toString() : "");
            }
            if (payload.containsKey("topic")) {
                session.setSessionType(payload.get("topic") != null ? payload.get("topic").toString() : session.getSessionType());
            }
            if (payload.containsKey("preferredDate")) {
                session.setSessionDate(payload.get("preferredDate") != null ? payload.get("preferredDate").toString() : session.getSessionDate());
            }
            if (payload.containsKey("preferredTime")) {
                session.setSessionTime(payload.get("preferredTime") != null ? payload.get("preferredTime").toString() : session.getSessionTime());
            }
            if (payload.containsKey("trainerEmail")) {
                String tEmail = payload.get("trainerEmail") != null ? payload.get("trainerEmail").toString().trim() : "";
                if (!tEmail.isEmpty()) {
                    userRepository.findByEmail(tEmail).ifPresent(session::setMentor);
                }
            }
            if (payload.containsKey("email")) {
                String cEmail = payload.get("email") != null ? payload.get("email").toString().trim() : "";
                if (!cEmail.isEmpty()) {
                    userRepository.findByEmail(cEmail).ifPresent(session::setStudent);
                }
            }

            MentorSession saved = mentorSessionRepository.save(session);
            return mapMentorSessionToMock(saved);
        }

        String email = securityUtils.getCurrentUserEmail();

        MockInterviewRequest request = mockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mock interview request not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.SUPER_ADMIN) {
            if (request.getTrainer() == null || !email.equals(request.getTrainer().getEmail())) {
                throw new RuntimeException("Access denied");
            }
        }

        String statusValue = payload.getOrDefault("status", request.getStatus()).toString();

        MockInterviewStatus status;
        try {
            status = MockInterviewStatus.valueOf(statusValue);
        } catch (Exception e) {
            throw new RuntimeException("Invalid mock interview status");
        }

        String meetingLink = payload.containsKey("meetingLink") ? (payload.get("meetingLink") != null ? payload.get("meetingLink").toString().trim() : "") : (request.getMeetingLink() != null ? request.getMeetingLink() : "");
        String remarks = payload.containsKey("trainerRemarks") ? (payload.get("trainerRemarks") != null ? payload.get("trainerRemarks").toString().trim() : "") : (request.getTrainerRemarks() != null ? request.getTrainerRemarks() : "");
        String summary = payload.containsKey("sessionSummary") ? (payload.get("sessionSummary") != null ? payload.get("sessionSummary").toString() : null) : request.getSessionSummary();
        String chat = payload.containsKey("sessionChat") ? (payload.get("sessionChat") != null ? payload.get("sessionChat").toString() : null) : request.getSessionChat();

        if (status == MockInterviewStatus.SCHEDULED && meetingLink.isBlank()) {
            throw new RuntimeException("Meeting link is required to schedule interview");
        }

        request.setStatus(status);
        request.setMeetingLink(meetingLink);
        request.setTrainerRemarks(remarks);
        request.setSessionSummary(summary);
        request.setSessionChat(chat);
        request.setUpdatedAt(LocalDateTime.now());

        if (payload.containsKey("topic")) {
            request.setTopic(payload.get("topic") != null ? payload.get("topic").toString() : request.getTopic());
        }
        if (payload.containsKey("student")) {
            request.setCandidateName(payload.get("student") != null ? payload.get("student").toString().trim() : "");
        }
        if (payload.containsKey("email")) {
            String cEmail = payload.get("email") != null ? payload.get("email").toString().trim() : "";
            request.setCandidateEmail(cEmail);
            if (!cEmail.isEmpty()) {
                userRepository.findByEmail(cEmail).ifPresentOrElse(
                    request::setStudent,
                    () -> request.setStudent(null)
                );
            } else {
                request.setStudent(null);
            }
        }
        if (payload.containsKey("trainerName")) {
            request.setHostName(payload.get("trainerName") != null ? payload.get("trainerName").toString().trim() : "");
        }
        if (payload.containsKey("trainerEmail")) {
            String tEmail = payload.get("trainerEmail") != null ? payload.get("trainerEmail").toString().trim() : "";
            request.setHostEmail(tEmail);
            if (!tEmail.isEmpty()) {
                userRepository.findByEmail(tEmail).ifPresentOrElse(
                    request::setTrainer,
                    () -> request.setTrainer(null)
                );
            } else {
                request.setTrainer(null);
            }
        }

        if (payload.containsKey("expirationDate")) {
            String expStr = payload.get("expirationDate") != null ? payload.get("expirationDate").toString() : null;
            request.setExpirationDate(parseDateTimeSafe(expStr));
        }
        if (payload.containsKey("isPublic")) {
            request.setIsPublic(Boolean.valueOf(payload.get("isPublic").toString()));
        }
        if (payload.containsKey("maxDurationMinutes")) {
            request.setMaxDurationMinutes(payload.get("maxDurationMinutes") != null ? Integer.valueOf(payload.get("maxDurationMinutes").toString()) : null);
        }
        if (payload.containsKey("actualDurationMinutes")) {
            request.setActualDurationMinutes(payload.get("actualDurationMinutes") != null ? Integer.valueOf(payload.get("actualDurationMinutes").toString()) : null);
        }
        if (payload.containsKey("isEnded")) {
            request.setIsEnded(Boolean.valueOf(payload.get("isEnded").toString()));
        }
        if (payload.containsKey("participantCount")) {
            request.setParticipantCount(payload.get("participantCount") != null ? Integer.valueOf(payload.get("participantCount").toString()) : null);
        }
        if (payload.containsKey("meetingLogs")) {
            request.setMeetingLogs(payload.get("meetingLogs") != null ? payload.get("meetingLogs").toString() : null);
        }

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

    private LocalDateTime parseDateTimeSafe(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            if (str.contains("Z") || str.contains("+") || (str.lastIndexOf("-") > 7)) {
                return java.time.OffsetDateTime.parse(str).toLocalDateTime();
            }
            return LocalDateTime.parse(str);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(str.substring(0, 19));
            } catch (Exception e2) {
                return null;
            }
        }
    }

    public Map<String, Object> mapMock(MockInterviewRequest request) {
        Map<String, Object> map = new HashMap<>();

        map.put("id", request.getId());
        map.put("student", request.getStudent() != null ? request.getStudent().getName() : (request.getCandidateName() != null && !request.getCandidateName().isEmpty() ? request.getCandidateName() : "Guest Candidate"));
        map.put("email", request.getStudent() != null ? request.getStudent().getEmail() : (request.getCandidateEmail() != null && !request.getCandidateEmail().isEmpty() ? request.getCandidateEmail() : "Guest Email"));
        map.put("batch", request.getBatch() == null ? "Batch" : request.getBatch().getName());
        map.put("batchId", request.getBatch() == null ? null : request.getBatch().getId());
        map.put("topic", request.getTopic() == null ? "Mock Interview" : request.getTopic());
        map.put("preferredDate", request.getPreferredDate() == null ? "" : request.getPreferredDate());
        map.put("preferredTime", request.getPreferredTime() == null ? "" : request.getPreferredTime());
        map.put("notes", request.getNotes() == null ? "" : request.getNotes());
        map.put("status", request.getStatus() == null ? MockInterviewStatus.REQUESTED : request.getStatus());
        map.put("meetingLink", request.getMeetingLink() == null ? "" : request.getMeetingLink());
        map.put("trainerRemarks", request.getTrainerRemarks() == null ? "" : request.getTrainerRemarks());
        map.put("sessionSummary", request.getSessionSummary() == null ? "" : request.getSessionSummary());
        map.put("sessionChat", request.getSessionChat() == null ? "" : request.getSessionChat());
        map.put("createdAt", request.getCreatedAt());
        map.put("updatedAt", request.getUpdatedAt());
        map.put("expirationDate", request.getExpirationDate());
        map.put("maxDurationMinutes", request.getMaxDurationMinutes() == null ? 60 : request.getMaxDurationMinutes());
        map.put("actualDurationMinutes", request.getActualDurationMinutes() == null ? 0 : request.getActualDurationMinutes());
        map.put("isEnded", Boolean.TRUE.equals(request.getIsEnded()));
        map.put("participantCount", request.getParticipantCount() == null ? 0 : request.getParticipantCount());
        map.put("meetingLogs", request.getMeetingLogs() == null ? "" : request.getMeetingLogs());
        map.put("isPublic", Boolean.TRUE.equals(request.getIsPublic()));

        map.put("joinCount", request.getJoinCount() == null ? 0 : request.getJoinCount());
        map.put("recurringType", request.getRecurringType() == null ? "ONCE" : request.getRecurringType());
        map.put("recurringDays", request.getRecurringDays() == null ? "" : request.getRecurringDays());
        map.put("invitedEmails", request.getInvitedEmails() == null ? "" : request.getInvitedEmails());
        map.put("preferredEndTime", request.getPreferredEndTime() == null ? "" : request.getPreferredEndTime().toString());
        map.put("timezone", request.getTimezone() == null ? "Asia/Kolkata" : request.getTimezone());

        java.util.List<Map<String, Object>> historyList = new java.util.ArrayList<>();
        if (request.getId() != null) {
            joinHistoryRepository.findByMockInterviewIdOrderByJoinedAtDesc(request.getId()).forEach(h -> {
                Map<String, Object> hMap = new HashMap<>();
                hMap.put("id", h.getId());
                hMap.put("name", h.getJoinedByName());
                hMap.put("email", h.getJoinedByEmail());
                hMap.put("role", h.getJoinedByRole());
                hMap.put("joinedAt", h.getJoinedAt());
                historyList.add(hMap);
            });
        }
        map.put("joinHistory", historyList);

        String hRole = request.getHostRole();
        if (hRole == null || hRole.isBlank()) {
            hRole = "GUEST";
            if (request.getTrainer() != null) {
                hRole = request.getTrainer().getRole().name();
            }
        }
        map.put("hostRole", hRole);

        String resolvedTrainerEmail = request.getTrainer() != null ? request.getTrainer().getEmail() : (request.getHostEmail() != null ? request.getHostEmail() : "");
        String resolvedTrainerName = request.getTrainer() != null ? request.getTrainer().getName() : (request.getHostName() != null ? request.getHostName() : "Guest Host");
        map.put("trainerEmail", resolvedTrainerEmail);
        map.put("trainerName", resolvedTrainerName);

        return map;
    }

    public Map<String, Object> mapMentorSessionToMock(MentorSession session) {
        Map<String, Object> map = new HashMap<>();

        map.put("id", -session.getId());
        map.put("student", session.getStudent() != null ? session.getStudent().getName() : "Guest Candidate");
        map.put("email", session.getStudent() != null ? session.getStudent().getEmail() : "Guest Email");
        map.put("batch", "Mentor Roster");
        map.put("batchId", null);
        map.put("topic", session.getSessionType() != null ? session.getSessionType() : "Mentor Session");
        map.put("preferredDate", session.getSessionDate() != null ? session.getSessionDate() : "");
        map.put("preferredTime", session.getSessionTime() != null ? session.getSessionTime() : "");
        map.put("notes", "Mentor Session scheduled via roster booking.");
        
        String status = session.getStatus();
        map.put("status", status != null ? status : "SCHEDULED");
        map.put("meetingLink", session.getMeetingLink() != null ? session.getMeetingLink() : "");
        map.put("trainerRemarks", "Mentor: " + (session.getMentor() != null ? session.getMentor().getName() : "N/A"));
        map.put("sessionSummary", "");
        map.put("sessionChat", "");
        map.put("createdAt", session.getCreatedAt() != null ? session.getCreatedAt() : LocalDateTime.now());
        map.put("updatedAt", null);
        map.put("expirationDate", null);
        map.put("maxDurationMinutes", 60);
        map.put("actualDurationMinutes", 0);
        map.put("isEnded", "COMPLETED".equals(status));
        map.put("participantCount", 0);
        map.put("meetingLogs", "");
        map.put("hostRole", "MENTOR");
        map.put("trainerEmail", session.getMentor() != null ? session.getMentor().getEmail() : "");
        map.put("trainerName", session.getMentor() != null ? session.getMentor().getName() : "Mentor Assessor");

        return map;
    }

    public void deleteMockInterview(Long id) {
        String email = securityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.SUPER_ADMIN) {
            throw new RuntimeException("Access denied: Admin role required for deletion");
        }

        if (id < 0) {
            MentorSession session = mentorSessionRepository.findById(-id)
                    .orElseThrow(() -> new RuntimeException("Mentor session not found"));
            mentorSessionRepository.delete(session);
            return;
        }

        MockInterviewRequest request = mockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mock interview request not found"));

        mockRepository.delete(request);
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

    @Transactional
    public Map<String, Object> createPublicMockInterview(Map<String, Object> payload) {
        String topic = payload.getOrDefault("topic", "Live Connect Mock Interview").toString();
        String hostEmail = payload.getOrDefault("hostEmail", "").toString().trim();
        String hostName = payload.getOrDefault("hostName", "").toString().trim();
        String candidateEmail = payload.getOrDefault("candidateEmail", "").toString().trim();
        String candidateName = payload.getOrDefault("candidateName", "").toString().trim();
        
        Integer duration = 60;
        if (payload.containsKey("maxDurationMinutes") && payload.get("maxDurationMinutes") != null) {
            try {
                duration = Integer.valueOf(payload.get("maxDurationMinutes").toString());
            } catch (Exception e) {
                // ignore
            }
        }

        LocalDateTime expiration = LocalDateTime.now().plusDays(7);
        if (payload.containsKey("expirationDate") && payload.get("expirationDate") != null) {
            try {
                expiration = parseDateTimeSafe(payload.get("expirationDate").toString());
            } catch (Exception e) {
                // ignore
            }
        }

        MockInterviewRequest request = new MockInterviewRequest();
        request.setTopic(topic);
        request.setStatus(MockInterviewStatus.SCHEDULED);
        request.setMaxDurationMinutes(duration);
        request.setExpirationDate(expiration);
        request.setIsEnded(false);
        request.setIsPublic(true);
        request.setCreatedAt(LocalDateTime.now());

        String hostRole = payload.getOrDefault("hostRole", "GUEST").toString().trim().toUpperCase();
        request.setHostRole(hostRole);

        if (payload.containsKey("preferredDate") && payload.get("preferredDate") != null && !payload.get("preferredDate").toString().isBlank()) {
            try {
                request.setPreferredDate(LocalDate.parse(payload.get("preferredDate").toString()));
            } catch (Exception e) {}
        }
        if (payload.containsKey("preferredTime") && payload.get("preferredTime") != null && !payload.get("preferredTime").toString().isBlank()) {
            try {
                request.setPreferredTime(LocalTime.parse(payload.get("preferredTime").toString()));
            } catch (Exception e) {}
        }
        if (payload.containsKey("preferredEndTime") && payload.get("preferredEndTime") != null && !payload.get("preferredEndTime").toString().isBlank()) {
            try {
                request.setPreferredEndTime(LocalTime.parse(payload.get("preferredEndTime").toString()));
            } catch (Exception e) {}
        }
        if (payload.containsKey("recurringType") && payload.get("recurringType") != null) {
            request.setRecurringType(payload.get("recurringType").toString().trim().toUpperCase());
        }
        if (payload.containsKey("recurringDays") && payload.get("recurringDays") != null) {
            request.setRecurringDays(payload.get("recurringDays").toString());
        }
        if (payload.containsKey("invitedEmails") && payload.get("invitedEmails") != null) {
            request.setInvitedEmails(payload.get("invitedEmails").toString());
        }
        if (payload.containsKey("timezone") && payload.get("timezone") != null) {
            request.setTimezone(payload.get("timezone").toString());
        }

        if (!hostEmail.isEmpty()) {
            userRepository.findByEmail(hostEmail).ifPresent(request::setTrainer);
        }
        if (!candidateEmail.isEmpty()) {
            userRepository.findByEmail(candidateEmail).ifPresent(request::setStudent);
        }

        request.setHostName(hostName);
        request.setHostEmail(hostEmail);
        request.setCandidateName(candidateName);
        request.setCandidateEmail(candidateEmail);

        MockInterviewRequest saved = mockRepository.save(request);
        return mapMock(saved);
    }

    @Transactional
    public Map<String, Object> updatePublicMockInterviewTelemetry(Long id, Map<String, Object> payload) {
        MockInterviewRequest request = mockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mock interview request not found"));

        if (payload.containsKey("status")) {
            try {
                request.setStatus(MockInterviewStatus.valueOf(payload.get("status").toString()));
            } catch (Exception e) {
                // ignore
            }
        }
        if (payload.containsKey("sessionChat")) {
            request.setSessionChat(payload.get("sessionChat") != null ? payload.get("sessionChat").toString() : null);
        }
        if (payload.containsKey("sessionSummary")) {
            request.setSessionSummary(payload.get("sessionSummary") != null ? payload.get("sessionSummary").toString() : null);
        }
        if (payload.containsKey("isEnded")) {
            request.setIsEnded(Boolean.valueOf(payload.get("isEnded").toString()));
        }
        if (payload.containsKey("actualDurationMinutes")) {
            request.setActualDurationMinutes(payload.get("actualDurationMinutes") != null ? Integer.valueOf(payload.get("actualDurationMinutes").toString()) : null);
        }
        if (payload.containsKey("participantCount")) {
            request.setParticipantCount(payload.get("participantCount") != null ? Integer.valueOf(payload.get("participantCount").toString()) : null);
        }
        if (payload.containsKey("meetingLogs")) {
            request.setMeetingLogs(payload.get("meetingLogs") != null ? payload.get("meetingLogs").toString() : null);
        }
        if (payload.containsKey("trainerRemarks")) {
            request.setTrainerRemarks(payload.get("trainerRemarks") != null ? payload.get("trainerRemarks").toString() : null);
        }

        request.setUpdatedAt(LocalDateTime.now());
        MockInterviewRequest saved = mockRepository.save(request);
        return mapMock(saved);
    }

    @Transactional
    public void logMeetingJoin(Long sessionId, String name, String email, String role) {
        MockInterviewRequest request = mockRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Mock interview request not found"));
        
        Integer currentCount = request.getJoinCount();
        request.setJoinCount(currentCount == null ? 1 : currentCount + 1);
        mockRepository.save(request);

        MockInterviewJoinHistory join = new MockInterviewJoinHistory();
        join.setMockInterview(request);
        join.setJoinedByName(name);
        join.setJoinedByEmail(email);
        join.setJoinedByRole(role);
        join.setJoinedAt(LocalDateTime.now());
        joinHistoryRepository.save(join);
    }
}
