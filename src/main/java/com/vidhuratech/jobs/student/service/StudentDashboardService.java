package com.vidhuratech.jobs.student.service;

import com.vidhuratech.jobs.lms.batch.entity.BatchEnrollment;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.student.dto.StudentCourseDTO;
import com.vidhuratech.jobs.student.dto.StudentDashboardResponseDTO;
import com.vidhuratech.jobs.trainer.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final BatchEnrollmentRepository enrollmentRepository;
    private final AssessmentRepository assessmentRepository;

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

        return StudentCourseDTO.builder()
                .courseId(enrollment.getBatch().getCourse().getId())
                .courseName(enrollment.getBatch().getCourse().getTitle())
                .batchId(enrollment.getBatch().getId())
                .batchName(enrollment.getBatch().getName())
                .progress(0)
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
                        .map(enrollment ->
                                enrollment
                                        .getBatch()
                                        .getId()
                        )
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

        Map<String, Object> stats =
                new HashMap<>();

        stats.put(
                "enrolledCourses",
                enrolledCourses
        );

        stats.put(
                "attendance",
                0
        );

        stats.put(
                "assignmentsPending",
                0
        );

        stats.put(
                "assessmentsUpcoming",
                assessmentsUpcoming
        );

        stats.put(
                "certificates",
                0
        );

        stats.put(
                "placementStatus",
                "Not Eligible"
        );

        return stats;
    }
    private Map<String, List<?>> buildSections(List<StudentCourseDTO> myCourses) {

        Map<String, List<?>> sections = new HashMap<>();

        sections.put("myCourses", myCourses);
        sections.put("notifications", List.of());
        sections.put("mentorSessions", List.of());

        return sections;
    }
}