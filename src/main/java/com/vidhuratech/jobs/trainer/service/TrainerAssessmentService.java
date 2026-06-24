package com.vidhuratech.jobs.trainer.service;

import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;
import com.vidhuratech.jobs.trainer.entity.Assessment;
import com.vidhuratech.jobs.trainer.entity.AssessmentAnswer;
import com.vidhuratech.jobs.trainer.entity.AssessmentAttempt;
import com.vidhuratech.jobs.trainer.entity.AssessmentQuestion;
import com.vidhuratech.jobs.trainer.repository.AssessmentAnswerRepository;
import com.vidhuratech.jobs.trainer.repository.AssessmentAttemptRepository;
import com.vidhuratech.jobs.trainer.repository.AssessmentRepository;
import com.vidhuratech.jobs.user.repository.UserRepository;
import com.vidhuratech.jobs.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TrainerAssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final BatchRepository batchRepository;
    private final SecurityUtils securityUtils;
    private final AssessmentAnswerRepository answerRepository;
    private final ActivityNotificationService notificationService;
    private final UserRepository userRepository;


    @Transactional
    public Map<String, Object> createAssessment(
            Map<String, Object> payload
    ) {
        try {
            Long batchId = payload.get("batchId") == null || String.valueOf(payload.get("batchId")).isBlank()
                    ? 0L
                    : Long.valueOf(String.valueOf(payload.get("batchId")));

            String email = securityUtils.getCurrentUserEmail();
            Batch batch = null;
            User trainerUser = null;

            if (batchId != 0L) {
                batch = batchRepository
                        .findByIdAndTrainerEmail(batchId, email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Access denied. You can create assessments only for your assigned batches."
                                ));
                trainerUser = batch.getTrainer();
            } else {
                trainerUser = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Trainer user not found"));
            }

            boolean publicVisible = batchId == 0L || (payload.get("publicVisible") != null && Boolean.parseBoolean(String.valueOf(payload.get("publicVisible"))));

            Object askedYearObj = payload.get("askedYear");
            Integer askedYear = null;
            if (askedYearObj != null && !String.valueOf(askedYearObj).isBlank()) {
                try {
                    askedYear = Integer.valueOf(String.valueOf(askedYearObj));
                } catch (NumberFormatException ignored) {}
            }

            Assessment assessment = Assessment.builder()
                    .title(String.valueOf(payload.get("title")))
                    .description(String.valueOf(payload.get("description")))
                    .totalMarks(Integer.valueOf(
                            String.valueOf(payload.get("totalMarks"))
                    ))
                    .durationMinutes(Integer.valueOf(
                            String.valueOf(payload.get("durationMinutes"))
                    ))
                    .startTime(LocalDateTime.now())
                    .endTime(LocalDateTime.now().plusDays(30))
                    .batch(batch)
                    .trainer(trainerUser)
                    .active(true)
                    .companyName(String.valueOf(payload.getOrDefault("companyName", "General")))
                    .skill(String.valueOf(payload.getOrDefault("skill", "Placement Readiness")))
                    .askedYear(askedYear)
                    .publicVisible(publicVisible)
                    .publicAccessLevel(String.valueOf(payload.getOrDefault("publicAccessLevel", "LEAD_REQUIRED")))
                    .publicAttemptLimit(payload.get("publicAttemptLimit") == null ? 1 : Integer.valueOf(String.valueOf(payload.get("publicAttemptLimit"))))
                    .build();
            notificationService.notifyAdmins(
                    "New assessment posted",
                    "Trainer posted assessment: " + assessment.getTitle(),
                    "ASSESSMENT_CREATED",
                    "/dashboard/admin/public-practice"
            );

            if (assessment.getBatch() != null) {
                notificationService.notifyBatchStudents(
                        assessment.getBatch().getEnrollments(),
                        "New assessment assigned",
                        "New assessment added: " + assessment.getTitle(),
                        "ASSESSMENT_ASSIGNED",
                        "/dashboard/student/assessments"
                );
            }
            assessment = assessmentRepository.save(assessment);

            Object rawQuestions = payload.get("questions");

            if (!(rawQuestions instanceof List<?> rawList)) {
                throw new RuntimeException(
                        "Invalid questions payload"
                );
            }

            for (Object obj : rawList) {
                if (!(obj instanceof Map<?, ?> q)) {
                    throw new RuntimeException(
                            "Invalid question format"
                    );
                }

                Object rawOptions = q.get("options");
                if (!(rawOptions instanceof Map<?, ?> options)) {
                    throw new RuntimeException(
                            "Invalid options format"
                    );
                }

                AssessmentQuestion question =
                        AssessmentQuestion.builder()
                                .assessment(assessment)
                                .question(String.valueOf(q.get("question")))
                                .optionA(String.valueOf(options.get("A")))
                                .optionB(String.valueOf(options.get("B")))
                                .optionC(String.valueOf(options.get("C")))
                                .optionD(String.valueOf(options.get("D")))
                                .correctAnswer(String.valueOf(q.get("correctAnswer")).toUpperCase())
                                .marks(Integer.valueOf(String.valueOf(q.get("marks"))))
                                .explanation(
                                        q.get("explanation") == null
                                                ? ""
                                                : String.valueOf(q.get("explanation"))
                                )
                                .build();

                assessment.getQuestions().add(question);
            }

            assessmentRepository.save(assessment);

            Map<String, Object> response = new HashMap<>();
            response.put("assessmentId", assessment.getId());
            response.put("title", assessment.getTitle());
            response.put("questionsCount", assessment.getQuestions().size());
            response.put("message", "Assessment created successfully");
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create assessment: " + e.getMessage());
        }
    }
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTrainerAssessments() {
        String email = securityUtils.getCurrentUserEmail();

        return assessmentRepository.findByTrainerEmail(email)
                .stream()
                .map(this::mapAssessmentListItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAssessmentDetails(Long assessmentId) {
        String email = securityUtils.getCurrentUserEmail();

        Assessment assessment = assessmentRepository.findDetailedAssessment(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        if (assessment.getTrainer() == null || !email.equals(assessment.getTrainer().getEmail())) {
            throw new RuntimeException("Access denied");
        }

        Map<String, Object> map = mapAssessmentListItem(assessment);

        map.put("questions", assessment.getQuestions().stream().map(q -> {
            Map<String, Object> question = new LinkedHashMap<>();
            question.put("id", q.getId());
            question.put("question", q.getQuestion());
            question.put("options", Map.of(
                    "A", q.getOptionA() == null ? "" : q.getOptionA(),
                    "B", q.getOptionB() == null ? "" : q.getOptionB(),
                    "C", q.getOptionC() == null ? "" : q.getOptionC(),
                    "D", q.getOptionD() == null ? "" : q.getOptionD()
            ));
            question.put("correctAnswer", q.getCorrectAnswer());
            question.put("marks", q.getMarks() == null ? 0 : q.getMarks());
            question.put("explanation", q.getExplanation() == null ? "" : q.getExplanation());
            return question;
        }).toList());

        return map;
    }

    private Map<String, Object> mapAssessmentListItem(Assessment assessment) {
        Long attemptCount = attemptRepository.findByAssessmentId(assessment.getId())
                .stream()
                .count();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", assessment.getId());
        map.put("title", assessment.getTitle());
        map.put("description", assessment.getDescription());
        map.put("totalMarks", assessment.getTotalMarks() == null ? 0 : assessment.getTotalMarks());
        map.put("durationMinutes", assessment.getDurationMinutes() == null ? 0 : assessment.getDurationMinutes());
        map.put("active", Boolean.TRUE.equals(assessment.getActive()));
        map.put("createdAt", assessment.getStartTime());
        map.put("companyName", assessment.getCompanyName() == null ? "General" : assessment.getCompanyName());
        map.put("skill", assessment.getSkill() == null ? "Placement Readiness" : assessment.getSkill());
        map.put("askedYear", assessment.getAskedYear());
        map.put("publicVisible", Boolean.TRUE.equals(assessment.getPublicVisible()));
        map.put("publicAccessLevel", assessment.getPublicAccessLevel());
        map.put("publicAttemptLimit", assessment.getPublicAttemptLimit());
        map.put("publishedAt", assessment.getPublishedAt());

        if (assessment.getBatch() != null) {
            map.put("batchId", assessment.getBatch().getId());
            map.put("batchName", assessment.getBatch().getName());
            map.put(
                    "courseName",
                    assessment.getBatch().getCourse() == null
                            ? ""
                            : assessment.getBatch().getCourse().getTitle()
            );
        } else {
            map.put("batchId", null);
            map.put("batchName", "");
            map.put("courseName", "");
        }

        map.put("questionCount", assessment.getQuestions() == null ? 0 : assessment.getQuestions().size());
        map.put("attemptCount", attemptCount);

        return map;
    }

    public List<Map<String, Object>> getAssessmentAttempts(Long assessmentId) {
        return attemptRepository.findByAssessmentId(assessmentId)
                .stream()
                .map(attempt -> {
                    Map<String, Object> map = new HashMap<>();

                    int score = attempt.getScore() == null ? 0 : attempt.getScore();
                    int totalQuestions = attempt.getTotalQuestions() == null ? 0 : attempt.getTotalQuestions();
                    int totalMarks = attempt.getAssessment() == null || attempt.getAssessment().getTotalMarks() == null
                            ? 0
                            : attempt.getAssessment().getTotalMarks();
                    int percentage = totalMarks == 0 ? 0 : Math.round((score * 100f) / totalMarks);

                    map.put("id", attempt.getId());
                    map.put("studentName", attempt.getStudent() == null ? "Student" : attempt.getStudent().getName());
                    map.put("email", attempt.getStudent() == null ? "" : attempt.getStudent().getEmail());
                    map.put("totalScore", score);
                    map.put("totalMarks", totalMarks);
                    map.put("percentageScore", percentage);
                    map.put("correctAnswers", attempt.getCorrectAnswers() == null ? 0 : attempt.getCorrectAnswers());
                    map.put("totalQuestions", totalQuestions);
                    map.put("submittedAt", attempt.getSubmittedAt());

                    return map;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAssessmentAttemptDetails(Long assessmentId, Long attemptId) {

        AssessmentAttempt attempt = attemptRepository.findById(attemptId).orElseThrow(() ->
                                new RuntimeException("Attempt not found"));

        if (attempt.getAssessment() == null || !attempt.getAssessment().getId().equals(assessmentId)
        ) {
            throw new RuntimeException(
                    "Attempt does not belong to this assessment"
            );
        }

        List<AssessmentAnswer> answers = answerRepository.findByAttemptId(attemptId);

        List<Map<String, Object>> detailedAnswers =
                answers.stream()
                        .map(answer -> {

                            AssessmentQuestion q = answer.getQuestion();

                            Map<String, Object> item = new LinkedHashMap<>();

                            item.put("questionId", q.getId());
                            item.put("question", q.getQuestion());
                            item.put("questionText", q.getQuestion());
                            item.put("options", Map.of("A", q.getOptionA() == null ? "" : q.getOptionA(),
                                                        "B", q.getOptionB() == null ? "" : q.getOptionB(),
                                                        "C", q.getOptionC() == null ? "" : q.getOptionC(),
                                                        "D", q.getOptionD() == null ? "" : q.getOptionD()));
                            item.put("selectedOption", answer.getSelectedAnswer());
                            item.put("selectedAnswer", answer.getSelectedAnswer());
                            item.put("correctOption", q.getCorrectAnswer());
                            item.put("correctAnswer", q.getCorrectAnswer());
                            item.put("correct", Boolean.TRUE.equals(answer.getCorrect()));
                            item.put("marksAwarded", Boolean.TRUE.equals(answer.getCorrect()) ? q.getMarks() : 0);
                            item.put("marks", q.getMarks());
                            item.put("explanation", q.getExplanation() == null ? "" : q.getExplanation());
                            return item;
                        }).toList();

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("attempt", getAssessmentAttempts(assessmentId)
                        .stream()
                        .filter(item -> item.get("id").equals(attemptId))
                        .findFirst()
                        .orElse(Map.of())
        );
        response.put("answers", detailedAnswers);
        response.put("totalQuestions", detailedAnswers.size());
        response.put("correctAnswers", detailedAnswers.stream()
                        .filter(a -> Boolean.TRUE.equals(a.get("correct")))
                        .count());

        return response;
    }

    @Transactional
    public void deleteAssessment(Long assessmentId) {
        String email = securityUtils.getCurrentUserEmail();

        Assessment assessment = assessmentRepository.findDetailedAssessment(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        if (assessment.getTrainer() == null || !email.equals(assessment.getTrainer().getEmail())) {
            throw new RuntimeException("Access denied");
        }
        notificationService.notifyAdmins(
                "Assessment deleted",
                "Trainer deleted assessment: " + assessment.getTitle(),
                "ASSESSMENT_DELETED",
                "/dashboard/admin/public-practice"
        );
        assessmentRepository.delete(assessment);
    }
}