package com.vidhuratech.jobs.student.service;

import com.vidhuratech.jobs.lms.batch.entity.BatchEnrollment;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.student.dto.StudentCourseDTO;
import com.vidhuratech.jobs.student.dto.StudentDashboardResponseDTO;
import com.vidhuratech.jobs.trainer.dto.TrainingContentDTO;
import com.vidhuratech.jobs.trainer.entity.TrainingContent;
import com.vidhuratech.jobs.trainer.repository.AssessmentRepository;
import com.vidhuratech.jobs.trainer.repository.PseudoCodeChallengeRepository;
import com.vidhuratech.jobs.trainer.repository.TrainingContentRepository;
import com.vidhuratech.jobs.lms.progress.service.StudentProgressService;
import com.vidhuratech.jobs.trainer.repository.TrainingWorkItemRepository;
import com.vidhuratech.jobs.trainer.repository.TrainingSubmissionRepository;
import com.vidhuratech.jobs.trainer.entity.TrainingWorkItem;
import com.vidhuratech.jobs.trainer.entity.TrainingSubmission;
import com.vidhuratech.jobs.trainer.repository.PseudoCodeAttemptRepository;
import com.vidhuratech.jobs.trainer.entity.PseudoCodeAttempt;
import com.vidhuratech.jobs.user.repository.UserRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.certificate.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final BatchEnrollmentRepository enrollmentRepository;
    private final AssessmentRepository assessmentRepository;
    private final TrainingContentRepository contentRepository;
    private final PseudoCodeChallengeRepository pseudoCodeChallengeRepository;
    private final StudentProgressService progressService;
    private final TrainingWorkItemRepository workItemRepository;
    private final TrainingSubmissionRepository submissionRepository;
    private final PseudoCodeAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final CertificateRepository certificateRepository;

    @Transactional(readOnly = true)
    public StudentDashboardResponseDTO getDashboard() {

        String email = getCurrentUserEmail();

        long enrolledCourses =
                enrollmentRepository.countByStudentEmailAndActiveTrue(email);

        List<StudentCourseDTO> myCourses = getMyCourses();

        Map<String, Object> stats = buildStats(enrolledCourses);

        Map<String, List<?>> sections = buildSections(myCourses);

        return StudentDashboardResponseDTO.builder()
                .stats(stats)
                .sections(sections)
                .build();
    }

    @Transactional(readOnly = true)
    public List<StudentCourseDTO> getMyCourses() {

        String email = getCurrentUserEmail();

        List<BatchEnrollment> enrollments =
                enrollmentRepository.findActiveByStudentEmail(email);

        return enrollments.stream()
                .map(this::mapToCourseDTO)
                .toList();
    }

    private StudentCourseDTO mapToCourseDTO(BatchEnrollment enrollment) {

        if (enrollment.getBatch() == null ||
                enrollment.getBatch().getCourse() == null) {

            return StudentCourseDTO.builder()
                    .courseId(null)
                    .courseName("Course")
                    .batchId(null)
                    .batchName("Batch")
                    .progress(0)
                    .build();
        }

        int progress = 0;
        if (enrollment.getBatch().getId() != null) {
            progress = progressService.getProgress(enrollment.getBatch().getId());
        }

        return StudentCourseDTO.builder()
                .courseId(enrollment.getBatch().getCourse().getId())
                .courseName(enrollment.getBatch().getCourse().getTitle())
                .batchId(enrollment.getBatch().getId())
                .batchName(enrollment.getBatch().getName())
                .progress(progress)
                .build();
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    private Map<String, Object> buildStats(
            long enrolledCourses
    ) {

        String email =
                getCurrentUserEmail();

        List<Long> batchIds =
                enrollmentRepository
                        .findActiveByStudentEmail(email)
                        .stream()
                        .filter(enrollment -> enrollment.getBatch() != null && enrollment.getBatch().getId() != null)
                        .map(enrollment -> enrollment.getBatch().getId())
                        .toList();

        long assessmentsUpcoming = 0;

        if (!batchIds.isEmpty()) {

            assessmentsUpcoming =
                    assessmentRepository
                            .findActiveAssessmentsForStudent(
                                    batchIds,
                                    LocalDateTime.now()
                            )
                            .size();
        }

        long practiceItems = 0;
        long materials = 0;
        long notes = 0;
        long pseudoChallenges = 0;
        long assignmentsPending = 0;
        long solvedChallenges = 0;

        if (!batchIds.isEmpty()) {
            List<TrainingContent> content = contentRepository.findByBatchIdInOrderByCreatedAtDesc(batchIds);

            practiceItems = content.stream().filter(item -> item.getType().name().equals("PRACTICE")).count();
            materials = content.stream().filter(item -> item.getType().name().equals("MATERIAL")).count();
            notes = content.stream().filter(item -> item.getType().name().equals("NOTE")).count();
            pseudoChallenges = pseudoCodeChallengeRepository.countByBatchIdInAndActiveTrue(batchIds);

            // Compute actual pending assignments/work items
            List<TrainingWorkItem> workItems = workItemRepository.findForStudentBatches(batchIds);
            List<TrainingSubmission> submissions = submissionRepository.findByStudentEmailOrderBySubmittedAtDesc(email);
            Set<Long> submittedIds = submissions.stream()
                    .map(s -> s.getWorkItem().getId())
                    .collect(Collectors.toSet());
            assignmentsPending = workItems.stream()
                    .filter(item -> !submittedIds.contains(item.getId()))
                    .count();
        }

        User student = userRepository.findByEmail(email).orElse(null);
        if (student != null) {
            List<PseudoCodeAttempt> attempts = attemptRepository.findByStudentIdWithChallengeEagerly(student.getId());
            solvedChallenges = attempts.stream()
                    .filter(a -> "PASS".equals(a.getStatus()))
                    .map(a -> a.getChallenge().getId())
                    .distinct()
                    .count();
        }

        long certificatesCount = certificateRepository.findByEmail(email).size();

        // Calculate average course progress
        double avgProgress = 0.0;
        List<StudentCourseDTO> myCourses = getMyCourses();
        if (!myCourses.isEmpty()) {
            avgProgress = myCourses.stream()
                    .mapToInt(StudentCourseDTO::getProgress)
                    .average()
                    .orElse(0.0);
        }

        String placementStatus = "Not Eligible";
        if (avgProgress >= 70.0) {
            placementStatus = "Eligible";
        } else if (avgProgress >= 50.0) {
            placementStatus = "Almost Eligible";
        }

        Map<String, Object> stats =
                new HashMap<>();

        stats.put(
                "enrolledCourses",
                enrolledCourses
        );

        stats.put(
                "attendance",
                myCourses.isEmpty() ? 0 : (int)Math.min(95.0, 60.0 + avgProgress * 0.4)
        );

        stats.put("assignmentsPending", assignmentsPending);
        stats.put("practiceItems", practiceItems);
        stats.put("materials", materials);
        stats.put("notes", notes);
        stats.put("pseudoChallenges", pseudoChallenges);
        stats.put("solvedChallenges", solvedChallenges);

        stats.put(
                "assessmentsUpcoming",
                assessmentsUpcoming
        );

        stats.put(
                "certificates",
                certificatesCount
        );

        stats.put(
                "placementStatus",
                placementStatus
        );

        return stats;
    }
    private Map<String, List<?>> buildSections(List<StudentCourseDTO> myCourses) {
        Map<String, List<?>> sections = new HashMap<>();

        sections.put("myCourses", myCourses);
        sections.put("notifications", List.of());
        sections.put("mentorSessions", List.of());
        String email = getCurrentUserEmail();

        List<Long> batchIds = enrollmentRepository
                .findActiveByStudentEmail(email)
                .stream()
                .filter(enrollment -> enrollment.getBatch() != null && enrollment.getBatch().getId() != null)
                .map(enrollment -> enrollment.getBatch().getId())
                .toList();

        sections.put("learningContent",
                batchIds.isEmpty()
                        ? List.of()
                        : contentRepository.findByBatchIdInOrderByCreatedAtDesc(batchIds)
                        .stream()
                        .map(TrainingContentDTO::from)
                        .toList()
        );

        return sections;
    }

    @Transactional(readOnly = true)
    public List<TrainingContentDTO> getLearningContent() {
        String email = getCurrentUserEmail();

        List<Long> batchIds = enrollmentRepository
                .findActiveByStudentEmail(email)
                .stream()
                .filter(enrollment -> enrollment.getBatch() != null && enrollment.getBatch().getId() != null)
                .map(enrollment -> enrollment.getBatch().getId())
                .toList();

        if (batchIds.isEmpty()) {
            return List.of();
        }

        return contentRepository.findByBatchIdInOrderByCreatedAtDesc(batchIds)
                .stream()
                .map(TrainingContentDTO::from)
                .toList();
    }
}