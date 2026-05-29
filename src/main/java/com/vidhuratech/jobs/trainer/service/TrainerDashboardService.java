package com.vidhuratech.jobs.trainer.service;

import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.dashboard.dto.DashboardStatsResponse;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.course.entity.Course;
import com.vidhuratech.jobs.lms.course.entity.CourseTrainerAssignment;
import com.vidhuratech.jobs.lms.course.repository.CourseTrainerAssignmentRepository;
import com.vidhuratech.jobs.trainer.dto.TrainingContentDTO;
import com.vidhuratech.jobs.trainer.entity.*;
import com.vidhuratech.jobs.trainer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
    private final ActivityNotificationService notificationService;
    private final TrainingSubmissionRepository submissionRepository;
    private final MockInterviewRequestRepository mockRepository;
    private final TrainingContentRepository contentRepository;
    private final CourseTrainerAssignmentRepository courseTrainerAssignmentRepository;

    public DashboardStatsResponse getDashboard() {
        String email = securityUtils.getCurrentUserEmail();

        long assignedBatches = batchRepository.countByTrainerEmail(email);
        long totalStudents = enrollmentRepository.countStudentsByTrainerEmail(email);
        long pendingReviews = submissionRepository.countByTrainerEmailAndStatus(email, TrainingSubmissionStatus.SUBMITTED);
        long mockRequests = mockRepository.countByTrainerEmailAndStatus(email, MockInterviewStatus.REQUESTED);

        List<Map<String, Object>> batches = getBatches();

        long requestedMocks = mockRepository.countByTrainerEmailAndStatus(email, MockInterviewStatus.REQUESTED);
        long scheduledMocks = mockRepository.countByTrainerEmailAndStatus(email, MockInterviewStatus.SCHEDULED);
        long completedMocks = mockRepository.countByTrainerEmailAndStatus(email, MockInterviewStatus.COMPLETED);
        List<Map<String, Object>> courses = getAssignedCourses();

        Map<String, Object> stats = new HashMap<>();
        stats.put("assignedBatches", assignedBatches);
        stats.put("totalStudents", totalStudents);
        stats.put("pendingReviews", pendingReviews);
        stats.put("todaysSessions", scheduledMocks);
        stats.put("avgAttendance", 0);
        stats.put("assignmentsSubmitted", submissionRepository.findByTrainerEmail(email).size());
        stats.put("requestedMocks", requestedMocks);
        stats.put("scheduledMocks", scheduledMocks);
        stats.put("completedMocks", completedMocks);
        stats.put("practiceItems", contentRepository.countByTrainerEmailAndType(email, TrainingContentType.PRACTICE));
        stats.put("materials", contentRepository.countByTrainerEmailAndType(email, TrainingContentType.MATERIAL));
        stats.put("notes", contentRepository.countByTrainerEmailAndType(email, TrainingContentType.NOTE));
        stats.put("contentUploaded", contentRepository.countByTrainerEmail(email));
        stats.put("assignedCourses", courses.size());
        Map<String, List<?>> sections = new HashMap<>();
        sections.put("batches", batches);
        sections.put("upcomingSessions", workflowService.getMockInterviewRequests().stream().limit(5).toList());
        sections.put("studentActivities", workflowService.getSubmissions().stream().limit(5).toList());
        sections.put("courses", courses);
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
                    map.put("course", batch.getCourse() != null ? batch.getCourse().getTitle() : "Course");
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
                .orElseThrow(() -> new RuntimeException("This batch is not assigned to your trainer account"));

        Optional<Curriculum> existing = curriculumRepository.findByBatchId(batchId);

        Curriculum curriculum = existing.orElseGet(() -> Curriculum.builder()
                .batchId(batchId)
                .trainerEmail(email)
                .build());

        curriculum.setJsonData(json);
        curriculumRepository.save(curriculum);

        Batch batch = batchRepository.findById(batchId).orElse(null);
        if (batch != null) {
            notificationService.notifyBatchStudents(
                    batch.getEnrollments(),
                    "Curriculum updated",
                    "Curriculum updated for " + batch.getName(),
                    "CURRICULUM_UPDATED",
                    "/dashboard/student/lms"
            );
        }
    }

    public Optional<Curriculum> getCurriculum(Long batchId) {
        String email = securityUtils.getCurrentUserEmail();

        batchRepository.findByIdAndTrainerEmail(batchId, email)
                .orElseThrow(() -> new RuntimeException("This batch is not assigned to your trainer account"));

        return curriculumRepository.findByBatchId(batchId);
    }

    public String getCurriculumPreview(Long batchId) {
        return curriculumRepository.findByBatchId(batchId)
                .map(Curriculum::getJsonData)
                .orElse(null);
    }

    public TrainingContent uploadContent(
            Long batchId,
            TrainingContentType type,
            String title,
            String description,
            MultipartFile file,
            String jsonData,
            String links
    ) {
        String email = securityUtils.getCurrentUserEmail();

        batchRepository.findByIdAndTrainerEmail(batchId, email)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        if (jsonData != null && !jsonData.isBlank()) {
            try {
                objectMapper.readTree(jsonData);
            } catch (Exception e) {
                throw new RuntimeException("Invalid JSON format");
            }
        }

        if (links != null && !links.isBlank()) {
            try {
                objectMapper.readTree(links);
            } catch (Exception e) {
                throw new RuntimeException("Invalid links format");
            }
        }

        try {
            TrainingContent content = TrainingContent.builder()
                    .batchId(batchId)
                    .trainerEmail(email)
                    .type(type)
                    .title(title)
                    .description(description)
                    .jsonData(jsonData != null && !jsonData.isBlank() ? jsonData : null)
                    .links(links != null && !links.isBlank() ? links : null)
                    .fileName(file != null && !file.isEmpty() ? file.getOriginalFilename() : null)
                    .fileType(file != null && !file.isEmpty() ? file.getContentType() : null)
                    .fileData(file != null && !file.isEmpty() ? file.getBytes() : null)
                    .createdAt(LocalDateTime.now())
                    .build();

            TrainingContent saved = contentRepository.save(content);

            Batch batch = batchRepository.findById(batchId).orElse(null);
            if (batch != null) {
                notificationService.notifyBatchStudents(
                        batch.getEnrollments(),
                        "New learning content",
                        "Trainer uploaded " + saved.getType() + ": " + saved.getTitle(),
                        "CONTENT_UPLOADED",
                        "/dashboard/student/lms"
                );

                notificationService.notifyAdmins(
                        "Trainer uploaded content",
                        saved.getTitle() + " uploaded for batch " + batch.getName(),
                        "CONTENT_UPLOADED",
                        "/dashboard/admin/batches"
                );
            }
            return saved;
        } catch (Exception e) {
            throw new RuntimeException("Unable to upload content");
        }
    }

    public List<TrainingContentDTO> getTrainerContent() {
        String email = securityUtils.getCurrentUserEmail();

        return contentRepository.findByTrainerEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(TrainingContentDTO::from)
                .toList();
    }

    public TrainingContent getContentFile(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));
    }

    public List<Map<String, Object>> getAssignedCourses() {
        String email = securityUtils.getCurrentUserEmail();

        return courseTrainerAssignmentRepository.findActiveDetailedByTrainerEmail(email)
                .stream()
                .map(this::mapAssignedCourse)
                .toList();
    }

    private Map<String, Object> mapAssignedCourse(CourseTrainerAssignment assignment) {
        Course course = assignment.getCourse();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("assignmentId", assignment.getId());
        map.put("courseId", assignment.getCourseId());
        map.put("title", course == null ? "Course" : course.getTitle());
        map.put("code", course == null ? "" : course.getCode());
        map.put("description", course == null ? "" : course.getDescription());
        map.put("level", course == null ? "" : course.getLevel());
        map.put("durationHours", course == null ? 0 : course.getDurationHours());
        map.put("price", course == null ? 0 : course.getPrice());
        map.put("status", course == null ? "" : course.getStatus());
        map.put("thumbnailUrl", course == null ? "" : course.getThumbnailUrl());
        map.put("assignedAt", assignment.getAssignedAt());
        map.put("autoMonthlyBatchEnabled", course != null && Boolean.TRUE.equals(course.getAutoMonthlyBatchEnabled()));

        return map;
    }
}
