package com.vidhuratech.jobs.lms.batch.service.impl;

import com.github.dockerjava.api.exception.BadRequestException;
import com.vidhuratech.jobs.common.exception.ResourceNotFoundException;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.batch.dto.*;
import com.vidhuratech.jobs.lms.batch.entity.*;
import com.vidhuratech.jobs.lms.batch.repository.*;
import com.vidhuratech.jobs.lms.batch.service.BatchService;
import com.vidhuratech.jobs.lms.course.repository.CourseRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final BatchSessionRepository sessionRepository;
    private final BatchEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final SecurityUtils securityUtils;

    @Override
    public BatchResponseDTO getBatchById(Long id) {

        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        return BatchResponseDTO.builder()
                .id(batch.getId())
                .name(batch.getName())
                .courseName(batch.getCourse().getTitle())
                .trainerName(batch.getTrainer().getName())
                .startDate(batch.getStartDate())
                .endDate(batch.getEndDate())
                .status(batch.getStatus().name())
                .build();
    }

    private Batch getTrainerOwnedBatch(Long id) {

        String email = securityUtils.getCurrentUserEmail();

        return batchRepository.findByIdAndTrainerEmail(id, email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Batch not found or access denied"
                        ));
    }


    @Override
    public List<BatchSessionResponseDTO> getSessions(Long batchId) {
        getBatchById(batchId);

        return sessionRepository.findByBatchIdOrderBySessionDateDesc(batchId)
                .stream()
                .map(session -> BatchSessionResponseDTO.builder()
                        .id(session.getId())
                        .title(session.getTitle())
                        .description(session.getDescription())
                        .videoUrl(session.getVideoUrl())
                        .durationMinutes(session.getDurationMinutes())
                        .sessionDate(session.getSessionDate())
                        .published(session.getPublished())
                        .build())
                .toList();
    }

    @Override
    public BatchSessionResponseDTO createSession(Long batchId, BatchSessionRequestDTO dto) {

        Batch batch = getTrainerOwnedBatch(batchId);
        BatchSession session = BatchSession.builder()
                .batch(batch)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .videoUrl(dto.getVideoUrl())
                .durationMinutes(dto.getDurationMinutes())
                .sessionDate(dto.getSessionDate())
                .published(dto.getPublished())
                .build();

        BatchSession saved = sessionRepository.save(session);

        return BatchSessionResponseDTO.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .videoUrl(saved.getVideoUrl())
                .durationMinutes(saved.getDurationMinutes())
                .sessionDate(saved.getSessionDate())
                .published(saved.getPublished())
                .build();
    }

    @Override
    public void publishSession(Long batchId, Long sessionId) {
        BatchSession session = getSession(batchId, sessionId);
        session.setPublished(true);
        sessionRepository.save(session);
    }

    @Override
    public void unpublishSession(Long batchId, Long sessionId) {
        BatchSession session = getSession(batchId, sessionId);
        session.setPublished(false);
        sessionRepository.save(session);
    }

    @Override
    public void deleteSession(Long batchId, Long sessionId) {
        BatchSession session = getSession(batchId, sessionId);
        sessionRepository.delete(session);
    }

    private BatchSession getSession(Long batchId, Long sessionId) {
        BatchSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getBatch().getId().equals(batchId)) {
            throw new ResourceNotFoundException("Session does not belong to batch");
        }

        return session;
    }

    @Override
    public void enrollStudent(Long batchId, Long studentId) {

        if (enrollmentRepository.existsByBatchIdAndStudentId(batchId, studentId)) {
            throw new BadRequestException("Student already enrolled");
        }

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (student.getRole() != UserRole.STUDENT) {
            throw new BadRequestException("Selected user is not a student");
        }

        BatchEnrollment enrollment = BatchEnrollment.builder()
                .batch(batch)
                .student(student)
                .active(true)
                .enrolledAt(LocalDateTime.now())
                .build();

        enrollmentRepository.save(enrollment);
    }

    @Override
    public void removeEnrollment(Long enrollmentId) {
        enrollmentRepository.deleteById(enrollmentId);
    }

    @Override
    public List<?> getEnrollments(Long batchId) {
        return enrollmentRepository.findByBatchId(batchId)
                .stream()
                .map(e -> Map.of(
                        "id", e.getId(),
                        "student", Map.of(
                                "id", e.getStudent().getId(),
                                "name", e.getStudent().getName(),
                                "email", e.getStudent().getEmail()
                        ),
                        "enrolledAt", e.getEnrolledAt()
                ))
                .toList();
    }

    @Transactional
    public Batch createBatch(BatchRequestDTO dto) {

        // 🔥 deactivate old active batch
        batchRepository.findActiveBatch(dto.getCourseId())
                .ifPresent(old -> {
                    old.setStatus(BatchStatus.COMPLETED);
                    old.setActive(false);
                    batchRepository.save(old);
                });

        Batch batch = Batch.builder()
                .name(dto.getName())
                .course(courseRepository.findById(dto.getCourseId()).orElseThrow())
                .trainer(userRepository.findById(dto.getTrainerId()).orElseThrow())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(BatchStatus.ACTIVE) // 🔥 auto active
                .active(true)
                .build();

        return batchRepository.save(batch);
    }

    @Override
    public Batch getActiveBatchByCourse(Long courseId) {

        return batchRepository
                .findTopByCourseIdAndStatusAndActiveTrueOrderByStartDateDesc(
                        courseId,
                        BatchStatus.ACTIVE
                )
                .orElse(null);
    }
}