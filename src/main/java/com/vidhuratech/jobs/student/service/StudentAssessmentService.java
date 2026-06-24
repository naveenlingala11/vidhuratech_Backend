package com.vidhuratech.jobs.student.service;

import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.batch.entity.BatchEnrollment;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.trainer.entity.Assessment;
import com.vidhuratech.jobs.trainer.entity.AssessmentAnswer;
import com.vidhuratech.jobs.trainer.entity.AssessmentAttempt;
import com.vidhuratech.jobs.trainer.entity.AssessmentQuestion;
import com.vidhuratech.jobs.trainer.repository.AssessmentAnswerRepository;
import com.vidhuratech.jobs.trainer.repository.AssessmentAttemptRepository;
import com.vidhuratech.jobs.trainer.repository.AssessmentQuestionRepository;
import com.vidhuratech.jobs.trainer.repository.AssessmentRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentAssessmentService {

    private final AssessmentRepository assessmentRepository;

    private final AssessmentAttemptRepository attemptRepository;

    private final AssessmentAnswerRepository answerRepository;

    private final AssessmentQuestionRepository questionRepository;

    private final UserRepository userRepository;

    private final BatchEnrollmentRepository
            batchEnrollmentRepository;

    private final SecurityUtils securityUtils;

    public List<Map<String, Object>> getStudentAssessments() {

        User student =
                userRepository.findByEmail(
                        securityUtils.getCurrentUserEmail()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Student not found"
                        ));

        List<BatchEnrollment> enrollments =
                batchEnrollmentRepository
                        .findActiveByStudentEmail(
                                student.getEmail()
                        );

        List<Long> batchIds =
                enrollments.stream()
                        .map(enrollment ->
                                enrollment
                                        .getBatch()
                                        .getId()
                        )
                        .toList();

        boolean hasBatches = !batchIds.isEmpty();

        return assessmentRepository
                .findActiveAssessmentsForStudentAndPublic(
                        hasBatches,
                        batchIds,
                        LocalDateTime.now()
                )
                .stream()
                .map(this::mapAssessment)
                .toList();
    }
    public Map<String, Object> getAssessment(Long id) {

        Assessment assessment =
                assessmentRepository
                        .findDetailedAssessment(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assessment not found"
                                ));

        User student =
                userRepository.findByEmail(
                        securityUtils.getCurrentUserEmail()
                ).orElseThrow(() ->
                        new RuntimeException("Student not found")
                );

        verifyStudentHasAssessmentAccess(student, assessment);

        Map<String, Object> map =
                new HashMap<>();

        map.put(
                "id",
                assessment.getId()
        );

        map.put(
                "companyName",
                assessment.getCompanyName()
        );

        map.put(
                "skill",
                assessment.getSkill()
        );

        map.put(
                "askedYear",
                assessment.getAskedYear()
        );

        map.put(
                "title",
                assessment.getTitle()
        );

        map.put(
                "description",
                assessment.getDescription()
        );

        map.put(
                "durationMinutes",
                assessment.getDurationMinutes()
        );

        map.put(
                "totalMarks",
                assessment.getTotalMarks()
        );

        map.put(
                "questions",
                assessment.getQuestions()
                        .stream()
                        .map(q -> {

                            Map<String, Object> qm =
                                    new HashMap<>();

                            qm.put(
                                    "id",
                                    q.getId()
                            );

                            qm.put(
                                    "question",
                                    q.getQuestion()
                            );

                            qm.put(
                                    "marks",
                                    q.getMarks()
                            );

                            qm.put(
                                    "options",
                                    Map.of(
                                            "A",
                                            q.getOptionA(),
                                            "B",
                                            q.getOptionB(),
                                            "C",
                                            q.getOptionC(),
                                            "D",
                                            q.getOptionD()
                                    )
                            );

                            return qm;
                        })
                        .toList()
        );

        return map;
    }

    @Transactional
    public Map<String, Object> submitAssessment(
            Long assessmentId,
            Map<String, Object> payload
    ) {

        User student =
                userRepository.findByEmail(
                        securityUtils.getCurrentUserEmail()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Student not found"
                        ));

        Assessment assessment =
                assessmentRepository.findDetailedAssessment(
                        assessmentId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Assessment not found"
                        ));

        verifyStudentHasAssessmentAccess(student, assessment);

        Object rawAnswers =
                payload.get("answers");

        if (!(rawAnswers instanceof List<?> rawList)) {

            throw new RuntimeException(
                    "Invalid answers payload"
            );
        }

        int totalScore = 0;

        int correctAnswers = 0;

        AssessmentAttempt attempt =
                AssessmentAttempt.builder()
                        .assessment(assessment)
                        .student(student)
                        .score(0)
                        .correctAnswers(0)
                        .totalQuestions(
                                assessment
                                        .getQuestions()
                                        .size()
                        )
                        .submittedAt(
                                LocalDateTime.now()
                        )
                        .build();

        attempt =
                attemptRepository.save(attempt);

        for (Object obj : rawList) {

            if (!(obj instanceof Map<?, ?> ans)) {
                continue;
            }

            Long questionId =
                    Long.valueOf(
                            String.valueOf(
                                    ans.get("questionId")
                            )
                    );

            String selectedAnswer =
                    String.valueOf(
                            ans.get("selectedAnswer")
                    );

            AssessmentQuestion question =
                    questionRepository
                            .findById(questionId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Question not found"
                                    ));

            boolean correct =
                    question.getCorrectAnswer()
                            .equalsIgnoreCase(
                                    selectedAnswer
                            );

            if (correct) {

                totalScore +=
                        question.getMarks();

                correctAnswers++;
            }

            AssessmentAnswer answer =
                    AssessmentAnswer.builder()
                            .attempt(attempt)
                            .question(question)
                            .selectedAnswer(
                                    selectedAnswer
                            )
                            .correct(correct)
                            .build();

            answerRepository.save(answer);
        }

        attempt.setScore(totalScore);

        attempt.setCorrectAnswers(
                correctAnswers
        );

        attemptRepository.save(attempt);

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "assessmentId",
                assessment.getId()
        );

        response.put(
                "score",
                totalScore
        );

        response.put(
                "correctAnswers",
                correctAnswers
        );

        response.put(
                "totalQuestions",
                assessment.getQuestions().size()
        );

        response.put(
                "percentage",
                (
                        totalScore * 100
                ) / assessment.getTotalMarks()
        );

        response.put(
                "submittedAt",
                attempt.getSubmittedAt()
        );

        return response;
    }

    private Map<String, Object> mapAssessment(
            Assessment assessment
    ) {

        User student =
                userRepository.findByEmail(
                        securityUtils.getCurrentUserEmail()
                ).orElseThrow();

        List<AssessmentAttempt> attempts =
                attemptRepository
                        .findByAssessmentIdAndStudentIdOrderByIdDesc(
                                assessment.getId(),
                                student.getId()
                        );

        AssessmentAttempt latestAttempt =
                attempts.isEmpty()
                        ? null
                        : attempts.get(0);

        Map<String, Object> map =
                new HashMap<>();

        map.put(
                "id",
                assessment.getId()
        );

        map.put(
                "title",
                assessment.getTitle()
        );

        map.put(
                "companyName",
                assessment.getCompanyName()
        );

        map.put(
                "skill",
                assessment.getSkill()
        );

        map.put(
                "askedYear",
                assessment.getAskedYear()
        );

        map.put(
                "description",
                assessment.getDescription()
        );

        map.put(
                "totalMarks",
                assessment.getTotalMarks()
        );

        map.put(
                "durationMinutes",
                assessment.getDurationMinutes()
        );

        map.put(
                "questionCount",
                assessment.getQuestions().size()
        );

        map.put(
                "attemptCount",
                attempts.size()
        );

        map.put(
                "lastScore",
                latestAttempt == null
                        ? 0
                        : latestAttempt.getScore()
        );

        double percentage =
                latestAttempt == null
                        ? 0
                        : (
                        (
                        double)
                                latestAttempt.getScore()
                        /
                                assessment.getTotalMarks()
                ) * 100;

        map.put(
                "percentage",
                Math.round(percentage)
        );

        map.put(
                "status",
                percentage >= 40
                        ? "PASS"
                        : "FAIL"
        );

        map.put(
                "lastSubmittedAt",
                latestAttempt == null
                        ? null
                        : latestAttempt.getSubmittedAt()
        );

        return map;
    }

    private void verifyStudentHasAssessmentAccess(
            User student,
            Assessment assessment
    ) {
        if (assessment.getBatch() == null || Boolean.TRUE.equals(assessment.getPublicVisible())) {
            return;
        }

        boolean enrolled =
                batchEnrollmentRepository
                        .findActiveByStudentEmail(student.getEmail())
                        .stream()
                        .anyMatch(enrollment ->
                                enrollment.getBatch() != null
                                        && enrollment.getBatch().getId()
                                        .equals(assessment.getBatch().getId())
                        );

        if (!enrolled) {
            throw new RuntimeException("Access denied");
        }
    }
}