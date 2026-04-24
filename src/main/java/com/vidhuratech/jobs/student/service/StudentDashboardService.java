package com.vidhuratech.jobs.student.service;

import com.vidhuratech.jobs.lms.batch.entity.BatchEnrollment;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.student.dto.StudentCourseDTO;
import com.vidhuratech.jobs.student.dto.StudentDashboardResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final BatchEnrollmentRepository enrollmentRepository;

    /* =========================
       🔹 PUBLIC METHODS
    ========================= */

    // ✅ Dashboard API
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

    // ✅ Courses API (used separately also)
    public List<StudentCourseDTO> getMyCourses() {

        String email = getCurrentUserEmail();

        List<BatchEnrollment> enrollments =
                enrollmentRepository.findActiveByStudentEmail(email);

        return enrollments.stream()
                .map(this::mapToCourseDTO)
                .toList();
    }

    // ✅ Assignments (future ready)
    public List<?> getAssignments() {
        return List.of(); // TODO: integrate assignments module
    }

    // ✅ Certificates (future ready)
    public List<?> getCertificates() {
        return List.of(); // TODO: integrate certificates module
    }

    /* =========================
       🔹 PRIVATE HELPERS
    ========================= */

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    private StudentCourseDTO mapToCourseDTO(BatchEnrollment enrollment) {

        return StudentCourseDTO.builder()
                .courseId(enrollment.getBatch().getCourse().getId())
                .courseName(enrollment.getBatch().getCourse().getTitle())

                .batchId(enrollment.getBatch().getId())
                .batchName(enrollment.getBatch().getName())

                .progress(0)
                .build();
    }

    private Map<String, Object> buildStats(long enrolledCourses) {

        Map<String, Object> stats = new HashMap<>();

        stats.put("enrolledCourses", enrolledCourses);
        stats.put("attendance", 0);
        stats.put("assignmentsPending", 0);
        stats.put("assessmentsUpcoming", 0);
        stats.put("certificates", 0);
        stats.put("placementStatus", "Not Eligible");

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