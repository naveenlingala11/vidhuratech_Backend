package com.vidhuratech.jobs.student.service;

import com.vidhuratech.jobs.common.security.SecurityUtils;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentAssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository questionRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final AssessmentAnswerRepository answerRepository;
    private final BatchEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public List<Map<String, Object>> getStudentAssessments() {

        User student = getCurrentStudent();

        List<Long> batchIds = enrollmentRepository
                .findActiveByStudentEmail(student.getEmail())
                .stream()
                .map(enrollment -> enrollment.getBatch().getId())
                .toList();

        if (batchIds.isEmpty()) {
            return List.of();
        }

        List<Assessment> assessments = assessmentRepository
                .findActiveAssessmentsForStudent(
                        batchIds,
                        LocalDateTime.now()
                );

        return assessments.stream()
                .map(assessment -> buildAssessmentCard(
                        assessment,
                        student.getId()
                ))
                .toList();
    }

    public Assessment getAssessmentById(Long assessmentId) {

        Assessment assessment = assessmentRepository
                .findById(assessmentId)
                .orElseThrow(() ->
                        new RuntimeException("Assessment not found"));

        validateAssessmentAvailability(assessment);

        return assessment;
    }

    public Map<String, Object> submitAssessment(
            Long assessmentId,
            Map<String, Object> payload
    ) {

        User student = getCurrentStudent();

        Assessment assessment = assessmentRepository
                .findById(assessmentId)
                .orElseThrow(() ->
                        new RuntimeException("Assessment not found"));

        validateAssessmentAvailability(assessment);

        boolean alreadySubmitted = attemptRepository
                .findByAssessmentIdAndStudentId(
                        assessmentId,
                        student.getId()
                )
                .isPresent();

        if (alreadySubmitted) {
            throw new RuntimeException("Assessment already submitted");
        }

        Object rawAnswers = payload.get("answers");

        if (!(rawAnswers instanceof List<?> rawList)) {
            throw new RuntimeException("Invalid answers payload");
        }

        int totalScore = 0;
        int correctAnswers = 0;

        AssessmentAttempt attempt = AssessmentAttempt.builder()
                .assessment(assessment)
                .student(student)
                .submittedAt(LocalDateTime.now())
                .build();

        attempt = attemptRepository.save(attempt);

        for (Object obj : rawList) {

            if (!(obj instanceof Map<?, ?> rawMap)) {
                throw new RuntimeException("Invalid answer format");
            }

            Long questionId = Long.valueOf(
                    String.valueOf(rawMap.get("questionId"))
            );

            String selectedAnswer = String.valueOf(
                    rawMap.get("selectedAnswer")
            ).toUpperCase();

            validateAnswerOption(selectedAnswer);

            AssessmentQuestion question = questionRepository
                    .findById(questionId)
                    .orElseThrow(() ->
                            new RuntimeException("Question not found"));

            validateQuestionOwnership(
                    question,
                    assessmentId
            );

            boolean correct = question.getCorrectAnswer()
                    .equalsIgnoreCase(selectedAnswer);

            if (correct) {
                totalScore += question.getMarks();
                correctAnswers++;
            }

            AssessmentAnswer assessmentAnswer = AssessmentAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .selectedAnswer(selectedAnswer)
                    .correct(correct)
                    .build();

            answerRepository.save(assessmentAnswer);
        }

        int totalQuestions = rawList.size();

        int percentage = assessment.getTotalMarks() == 0
                ? 0
                : (totalScore * 100) / assessment.getTotalMarks();

        int wrongAnswers = totalQuestions - correctAnswers;

        attempt.setScore(totalScore);
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setTotalQuestions(totalQuestions);

        attemptRepository.save(attempt);

        Map<String, Object> result = new HashMap<>();

        result.put("score", totalScore);
        result.put("totalMarks", assessment.getTotalMarks());
        result.put("percentage", percentage);
        result.put("correctAnswers", correctAnswers);
        result.put("wrongAnswers", wrongAnswers);
        result.put("totalQuestions", totalQuestions);
        result.put("passed", percentage >= 40);
        result.put("submittedAt", attempt.getSubmittedAt());

        return result;
    }

    // =========================
    // PRIVATE HELPERS
    // =========================

    private User getCurrentStudent() {

        String email = securityUtils.getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));
    }

    private void validateAssessmentAvailability(
            Assessment assessment
    ) {

        if (Boolean.FALSE.equals(assessment.getActive())) {
            throw new RuntimeException("Assessment inactive");
        }

        LocalDateTime now = LocalDateTime.now();

        if (assessment.getStartTime() != null &&
                now.isBefore(assessment.getStartTime())) {

            throw new RuntimeException(
                    "Assessment not started yet"
            );
        }

        if (assessment.getEndTime() != null &&
                now.isAfter(assessment.getEndTime())) {

            throw new RuntimeException(
                    "Assessment expired"
            );
        }
    }

    private void validateAnswerOption(
            String selectedAnswer
    ) {

        if (!List.of("A", "B", "C", "D")
                .contains(selectedAnswer)) {

            throw new RuntimeException(
                    "Invalid answer option"
            );
        }
    }

    private void validateQuestionOwnership(
            AssessmentQuestion question,
            Long assessmentId
    ) {

        if (!question.getAssessment()
                .getId()
                .equals(assessmentId)) {

            throw new RuntimeException(
                    "Question mismatch detected"
            );
        }
    }

    private Map<String, Object> buildAssessmentCard(
            Assessment assessment,
            Long studentId
    ) {

        AssessmentAttempt attempt = attemptRepository
                .findByAssessmentIdAndStudentId(
                        assessment.getId(),
                        studentId
                )
                .orElse(null);

        Map<String, Object> map = new HashMap<>();

        map.put("id", assessment.getId());
        map.put("title", assessment.getTitle());
        map.put("description", assessment.getDescription());
        map.put("batch", assessment.getBatch().getName());
        map.put("durationMinutes", assessment.getDurationMinutes());
        map.put("totalMarks", assessment.getTotalMarks());
        map.put("questionCount", assessment.getQuestions().size());
        map.put("attempted", attempt != null);
        map.put("score", attempt != null ? attempt.getScore() : 0);
        map.put(
                "submittedAt",
                attempt != null ? attempt.getSubmittedAt() : ""
        );

        return map;
    }
}