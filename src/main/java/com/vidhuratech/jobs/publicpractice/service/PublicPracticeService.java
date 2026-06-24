package com.vidhuratech.jobs.publicpractice.service;

import com.vidhuratech.jobs.common.exception.PracticeAccessRequiredException;
import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.leads.entity.Lead;
import com.vidhuratech.jobs.leads.repository.LeadRepository;
import com.vidhuratech.jobs.leads.service.LeadService;
import com.vidhuratech.jobs.plans.dto.UserPlanAccessDto;
import com.vidhuratech.jobs.plans.service.PlanAccessService;
import com.vidhuratech.jobs.prep.repository.InterviewQuestionRepository;
import com.vidhuratech.jobs.prep.entity.InterviewQuestion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vidhuratech.jobs.publicpractice.entity.PublicContestAnnouncement;
import com.vidhuratech.jobs.publicpractice.repository.PublicContestAnnouncementRepository;
import com.vidhuratech.jobs.student.service.CodeExecutionService;
import com.vidhuratech.jobs.student.service.CodeSecurityValidator;
import com.vidhuratech.jobs.trainer.entity.*;
import com.vidhuratech.jobs.trainer.repository.AssessmentRepository;
import com.vidhuratech.jobs.trainer.repository.PseudoCodeChallengeRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vidhuratech.jobs.publicpractice.entity.PublicAssessmentAttempt;
import com.vidhuratech.jobs.publicpractice.entity.PublicChallengeAttempt;
import com.vidhuratech.jobs.publicpractice.entity.PublicPracticeAccessGrant;
import com.vidhuratech.jobs.publicpractice.repository.PublicAssessmentAttemptRepository;
import com.vidhuratech.jobs.publicpractice.repository.PublicChallengeAttemptRepository;
import com.vidhuratech.jobs.publicpractice.repository.PublicPracticeAccessGrantRepository;

import java.security.SecureRandom;
import java.time.YearMonth;
import java.util.Base64;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PublicPracticeService {

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of(
            "JAVA", "PYTHON", "C", "CPP", "CSHARP", "FSHARP",
            "PHP", "RUBY", "HASKELL", "GO", "RUST", "TYPESCRIPT"
    );

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int PUBLIC_PRACTICE_ATTEMPT_LIMIT = 100;
    private final CodeSecurityValidator codeSecurityValidator;

    private final PublicPracticeAccessGrantRepository grantRepository;
    private final PublicAssessmentAttemptRepository publicAssessmentAttemptRepository;
    private final PublicChallengeAttemptRepository publicChallengeAttemptRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final AssessmentRepository assessmentRepository;
    private final PseudoCodeChallengeRepository challengeRepository;
    private final LeadService leadService;
    private final CodeExecutionService codeExecutionService;
    private final LeadRepository leadRepository;
    private final ActivityNotificationService notificationService;
    private final UserRepository userRepository;
    private final PublicContestAnnouncementRepository contestAnnouncementRepository;
    private final PlanAccessService planAccessService;
    private final ObjectMapper objectMapper;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public Map<String, Object> getPracticeLibrary() {
        List<Map<String, Object>> assessments = assessmentRepository
                .findActivePublicAssessments(LocalDateTime.now())
                .stream()
                .map(this::mapAssessmentCard)
                .toList();

        List<Map<String, Object>> challenges = challengeRepository
                .findByActiveTrueAndPublicVisibleTrueOrderByPublishedAtDesc()
                .stream()
                .map(this::mapChallengeCard)
                .toList();

        List<Map<String, Object>> interviewQuestions = interviewQuestionRepository
                .findActivePublicQuestionsForLibrary()
                .stream()
                .map(this::mapInterviewQuestionCard)
                .toList();

        Set<String> companies = new TreeSet<>();
        Set<String> skills = new TreeSet<>();

        assessments.forEach(item -> {
            companies.add(String.valueOf(item.get("company")));
            skills.add(String.valueOf(item.get("skill")));
        });

        challenges.forEach(item -> {
            companies.add(String.valueOf(item.get("company")));
            skills.add(String.valueOf(item.get("skill")));
        });

        interviewQuestions.forEach(item -> {
            companies.add(String.valueOf(item.get("company")));
            skills.add(String.valueOf(item.get("skill")));
        });

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("assessments", assessments);
        response.put("challenges", challenges);
        response.put("interviewQuestions", interviewQuestions);
        response.put("companies", companies);
        response.put("skills", skills);
        response.put("totalItems", assessments.size() + challenges.size() + interviewQuestions.size());

        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPublicAssessment(Long id) {
        Assessment assessment = assessmentRepository.findDetailedAssessment(id)
                .orElseThrow(() -> new RuntimeException("Mock test not found"));

        if (!Boolean.TRUE.equals(assessment.getActive())
                || !Boolean.TRUE.equals(assessment.getPublicVisible())) {
            throw new RuntimeException("This mock test is not available publicly");
        }

        Map<String, Object> map = new LinkedHashMap<>(mapAssessmentCard(assessment));

        map.put(
                "questions",
                assessment.getQuestions().stream().map(q -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", q.getId());
                    item.put("question", safe(q.getQuestion()));
                    item.put("marks", q.getMarks() == null ? 0 : q.getMarks());
                    item.put("options", Map.of(
                            "A", safe(q.getOptionA()),
                            "B", safe(q.getOptionB()),
                            "C", safe(q.getOptionC()),
                            "D", safe(q.getOptionD())
                    ));
                    return item;
                }).toList()
        );

        return map;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPracticeLibraryByCompany(String company) {
        Map<String, Object> library = getPracticeLibrary();

        List<Map<String, Object>> assessments =
                ((List<Map<String, Object>>) library.get("assessments"))
                        .stream()
                        .filter(item -> company.equalsIgnoreCase(String.valueOf(item.get("company"))))
                        .toList();

        List<Map<String, Object>> challenges =
                ((List<Map<String, Object>>) library.get("challenges"))
                        .stream()
                        .filter(item -> company.equalsIgnoreCase(String.valueOf(item.get("company"))))
                        .toList();

        List<Map<String, Object>> interviewQuestions =
                ((List<Map<String, Object>>) library.get("interviewQuestions"))
                        .stream()
                        .filter(item -> company.equalsIgnoreCase(String.valueOf(item.get("company"))))
                        .toList();

        Set<String> skills = new TreeSet<>();

        assessments.forEach(item -> skills.add(String.valueOf(item.get("skill"))));
        challenges.forEach(item -> skills.add(String.valueOf(item.get("skill"))));
        interviewQuestions.forEach(item -> skills.add(String.valueOf(item.get("skill"))));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("company", company);
        response.put("assessments", assessments);
        response.put("challenges", challenges);
        response.put("interviewQuestions", interviewQuestions);
        response.put("skills", skills);
        response.put("totalItems", assessments.size() + challenges.size() + interviewQuestions.size());

        return response;
    }

    @Transactional
    public Map<String, Object> submitPublicAssessment(
            Long assessmentId,
            Map<String, Object> payload
    ) {
        PublicPracticeAccessGrant grant = requireValidGrant(
                String.valueOf(payload.getOrDefault("accessToken", "")),
                "ASSESSMENT",
                assessmentId
        );

        Assessment assessment = assessmentRepository.findDetailedAssessment(assessmentId)
                .orElseThrow(() -> new RuntimeException("Mock test not found"));

        if (!Boolean.TRUE.equals(assessment.getActive())) {
            throw new RuntimeException("This mock test is not active");
        }

        Object rawAnswers = payload.get("answers");

        if (!(rawAnswers instanceof List<?> answers)) {
            throw new RuntimeException("Invalid answers payload");
        }

        Map<Long, String> submittedAnswers = new HashMap<>();

        for (Object obj : answers) {
            if (!(obj instanceof Map<?, ?> answer)) {
                continue;
            }

            Object rawQuestionId = answer.get("questionId");

            if (rawQuestionId == null) {
                continue;
            }

            Long questionId = Long.valueOf(String.valueOf(rawQuestionId));
            Object rawSelectedAnswer = answer.get("selectedAnswer");
            String selectedAnswer = rawSelectedAnswer == null
                    ? ""
                    : String.valueOf(rawSelectedAnswer).trim();
            submittedAnswers.put(questionId, selectedAnswer);
        }

        int score = 0;
        int correctAnswers = 0;

        List<Map<String, Object>> questionResults = new ArrayList<>();

        for (AssessmentQuestion q : assessment.getQuestions()) {
            String selectedAnswer = submittedAnswers.getOrDefault(q.getId(), "");
            String correctAnswer = q.getCorrectAnswer() == null ? "" : q.getCorrectAnswer();

            boolean correct = !selectedAnswer.isBlank()
                    && correctAnswer.equalsIgnoreCase(selectedAnswer);

            int marks = q.getMarks() == null ? 0 : q.getMarks();
            int marksObtained = correct ? marks : 0;

            if (correct) {
                score += marks;
                correctAnswers++;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("questionId", q.getId());
            item.put("question", safe(q.getQuestion()));
            item.put("options", Map.of(
                    "A", safe(q.getOptionA()),
                    "B", safe(q.getOptionB()),
                    "C", safe(q.getOptionC()),
                    "D", safe(q.getOptionD())
            ));
            item.put("selectedAnswer", selectedAnswer);
            item.put("correctAnswer", correctAnswer);
            item.put("isCorrect", correct);
            item.put("marks", marks);
            item.put("marksObtained", marksObtained);
            item.put("explanation", safe(q.getExplanation()));

            questionResults.add(item);
        }

        int totalMarks = assessment.getTotalMarks() == null ? 0 : assessment.getTotalMarks();
        int percentage = totalMarks == 0 ? 0 : Math.round((score * 100f) / totalMarks);
        PublicAssessmentAttempt attempt = PublicAssessmentAttempt.builder()
                .accessGrantId(grant.getId())
                .leadId(grant.getLeadId())
                .assessmentId(assessment.getId())
                .score(score)
                .totalMarks(totalMarks)
                .percentage(percentage)
                .correctAnswers(correctAnswers)
                .totalQuestions(assessment.getQuestions().size())
                .status(percentage >= 40 ? "PASS" : "FAIL")
                .submittedAt(LocalDateTime.now())
                .build();

        publicAssessmentAttemptRepository.save(attempt);
        markGrantUsed(grant);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("assessmentId", assessment.getId());
        response.put("score", score);
        response.put("totalMarks", totalMarks);
        response.put("percentage", percentage);
        response.put("correctAnswers", correctAnswers);
        response.put("totalQuestions", assessment.getQuestions().size());
        response.put("status", percentage >= 40 ? "PASS" : "FAIL");
        response.put("submittedAt", LocalDateTime.now());
        response.put("questionResults", questionResults);
        if (assessment.getTrainer() != null) {
            notificationService.notifyTrainer(
                    assessment.getTrainer(),
                    "Public assessment attempted",
                    "Someone attempted public assessment: " + assessment.getTitle(),
                    "PUBLIC_ASSESSMENT_ATTEMPTED",
                    "/dashboard/trainer/assessments"
            );
        }

        notificationService.notifyAdmins(
                "Public practice attempt",
                "Public assessment attempted: " + assessment.getTitle(),
                "PUBLIC_ASSESSMENT_ATTEMPTED",
                "/dashboard/admin/public-practice"
        );
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPublicChallenge(Long id) {
        PseudoCodeChallenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        if (!Boolean.TRUE.equals(challenge.getActive())
                || !Boolean.TRUE.equals(challenge.getPublicVisible())) {
            throw new RuntimeException("This challenge is not available publicly");
        }

        Map<String, Object> map = new LinkedHashMap<>(mapChallengeCard(challenge));

        map.put("problemStatement", safe(challenge.getProblemStatement()));
        map.put("constraintsText", safe(challenge.getConstraintsText()));
        map.put("inputFormat", safe(challenge.getInputFormat()));
        map.put("outputFormat", safe(challenge.getOutputFormat()));
        map.put("constraintsImageUrl", safe(challenge.getConstraintsImageUrl()));
        map.put("inputFormatImageUrl", safe(challenge.getInputFormatImageUrl()));
        map.put("outputFormatImageUrl", safe(challenge.getOutputFormatImageUrl()));
        map.put("hintText", safe(challenge.getHintText()));
        map.put("supportedLanguages", SUPPORTED_LANGUAGES);

        map.put(
                "sampleTestCases",
                challenge.getTestCases().stream()
                        .filter(tc -> !Boolean.TRUE.equals(tc.getHidden()))
                        .map(tc -> {
                            Map<String, Object> item = new LinkedHashMap<>();
                            item.put("inputData", safe(tc.getInputData()));
                            item.put("expectedOutput", safe(tc.getExpectedOutput()));
                            item.put("marks", tc.getMarks() == null ? 0 : tc.getMarks());
                            return item;
                        }).toList()
        );

        return map;
    }

    @Transactional
    public Map<String, Object> runPublicChallengeCustom(
            Long challengeId,
            Map<String, Object> payload
    ) {
        PublicPracticeAccessGrant grant = requireValidGrant(
                String.valueOf(payload.getOrDefault("accessToken", "")),
                "CHALLENGE",
                challengeId
        );
        PseudoCodeChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        if (!Boolean.TRUE.equals(challenge.getActive())) {
            throw new RuntimeException("This challenge is not active");
        }

        String language = String.valueOf(payload.getOrDefault("language", "")).trim().toUpperCase();
        String sourceCode = String.valueOf(payload.getOrDefault("sourceCode", ""));
        String customInput = String.valueOf(payload.getOrDefault("customInput", ""));

        if (!SUPPORTED_LANGUAGES.contains(language)) {
            throw new RuntimeException("Unsupported language");
        }

        codeSecurityValidator.validate(language, sourceCode);

        CodeExecutionService.ExecutionResult execution =
                codeExecutionService.run(language, sourceCode, customInput);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("challengeId", challenge.getId());
        response.put("customRun", true);
        response.put("status", execution.isSuccess() ? "SUCCESS" : "FAIL");
        response.put("inputData", customInput);
        response.put("actualOutput", safe(execution.getOutput()));
        response.put("errorMessage", safe(execution.getError()));
        response.put("executionTimeMs", execution.getExecutionTimeMs());

        return response;
    }

    @Transactional
    public Map<String, Object> runPublicChallenge(
            Long challengeId,
            Map<String, Object> payload
    ) {
        PublicPracticeAccessGrant grant = requireValidGrant(
                String.valueOf(payload.getOrDefault("accessToken", "")),
                "CHALLENGE",
                challengeId
        );
        PseudoCodeChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        if (!Boolean.TRUE.equals(challenge.getActive())) {
            throw new RuntimeException("This challenge is not active");
        }

        String language = String.valueOf(payload.getOrDefault("language", "")).trim().toUpperCase();
        String sourceCode = String.valueOf(payload.getOrDefault("sourceCode", ""));

        if (!SUPPORTED_LANGUAGES.contains(language)) {
            throw new RuntimeException("Unsupported language");
        }

        codeSecurityValidator.validate(language, sourceCode);

        int score = 0;
        List<Map<String, Object>> results = new ArrayList<>();
        long totalExecutionTimeMs = 0L;

        for (PseudoCodeRule rule : challenge.getRules()) {
            if (isRulePassed(rule, sourceCode)) {
                score += rule.getMarks() == null ? 0 : rule.getMarks();
            }
        }

        for (PseudoCodeTestCase testCase : challenge.getTestCases()) {
            CodeExecutionService.ExecutionResult execution =
                    codeExecutionService.run(language, sourceCode, testCase.getInputData());

            boolean passed = execution.isSuccess()
                    && normalize(execution.getOutput()).equals(normalize(testCase.getExpectedOutput()));

            int marks = passed ? testCase.getMarks() == null ? 0 : testCase.getMarks() : 0;
            score += marks;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("status", passed ? "PASS" : "FAIL");
            item.put(
                    "inputData",
                    Boolean.TRUE.equals(testCase.getHidden())
                            ? "Hidden Test Case"
                            : safe(testCase.getInputData())
            );
            item.put(
                    "expectedOutput",
                    Boolean.TRUE.equals(testCase.getHidden())
                            ? "Hidden"
                            : safe(testCase.getExpectedOutput())
            );
            item.put("actualOutput", safe(execution.getOutput()));
            item.put("errorMessage", safe(execution.getError()));
            item.put("marksObtained", marks);
            item.put("executionTimeMs", execution.getExecutionTimeMs());

            results.add(item);
            totalExecutionTimeMs += execution.getExecutionTimeMs() == null ? 0L : execution.getExecutionTimeMs();
        }

        int totalMarks = challenge.getTotalMarks() == null ? 0 : challenge.getTotalMarks();
        int percentage = totalMarks == 0 ? 0 : Math.round((score * 100f) / totalMarks);
        int passPercentage = passPercentageForDifficulty(challenge);
        String difficultyLevel = normalizedDifficulty(challenge);
        Lead lead = leadRepository.findById(grant.getLeadId()).orElse(null);

        Long matchedUserId = securityUtils.getCurrentUserId();

        if (matchedUserId == null && lead != null && lead.getEmail() != null && !lead.getEmail().isBlank()) {
            matchedUserId = userRepository.findByEmail(lead.getEmail().trim().toLowerCase())
                    .map(User::getId)
                    .orElse(null);
        }

        PublicChallengeAttempt attempt = PublicChallengeAttempt.builder()
                .accessGrantId(grant.getId())
                .leadId(grant.getLeadId())
                .challengeId(challenge.getId())
                .language(language)
                .sourceCode(sourceCode)
                .score(score)
                .totalMarks(totalMarks)
                .percentage(percentage)
                .status(percentage >= passPercentage ? "PASS" : "FAIL")
                .participantName(lead == null ? "" : lead.getName())
                .participantEmail(lead == null ? "" : lead.getEmail())
                .participantPhone(lead == null ? "" : lead.getPhone())
                .userId(matchedUserId)
                .totalExecutionTimeMs(totalExecutionTimeMs)
                .submittedAt(LocalDateTime.now())
                .build();

        publicChallengeAttemptRepository.save(attempt);
        markGrantUsed(grant);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("challengeId", challenge.getId());
        response.put("score", score);
        response.put("totalMarks", totalMarks);
        response.put("percentage", percentage);
        response.put("status", percentage >= passPercentage ? "PASS" : "FAIL");
        response.put("difficultyLevel", difficultyLevel);
        response.put("passPercentage", passPercentage);
        response.put("passed", percentage >= passPercentage);
        response.put("submittedAt", LocalDateTime.now());
        response.put("testResults", results);
        response.put("rankPreview", getParticipantRankPreview(challenge.getId(), attempt));
        userRepository.findByEmail(challenge.getTrainerEmail()).ifPresent(trainer ->
                notificationService.notifyTrainer(
                        trainer,
                        "Public challenge attempted",
                        "Someone attempted public challenge: " + challenge.getTitle(),
                        "PUBLIC_CHALLENGE_ATTEMPTED",
                        "/dashboard/trainer/pseudo-challenges"
                )
        );

        notificationService.notifyAdmins(
                "Public coding challenge attempt",
                "Public challenge attempted: " + challenge.getTitle(),
                "PUBLIC_CHALLENGE_ATTEMPTED",
                "/dashboard/admin/public-practice"
        );
        return response;
    }

    private Map<String, Object> mapAssessmentCard(Assessment assessment) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", assessment.getId());
        map.put("type", "ASSESSMENT");
        map.put("title", safe(assessment.getTitle()));
        map.put("description", safe(assessment.getDescription()));
        map.put("company", assessment.getCompanyName() == null || assessment.getCompanyName().isBlank()
                ? "General"
                : assessment.getCompanyName());
        map.put("skill", assessment.getSkill() == null || assessment.getSkill().isBlank()
                ? "Placement Readiness"
                : assessment.getSkill());
        map.put("durationMinutes", assessment.getDurationMinutes() == null ? 0 : assessment.getDurationMinutes());
        map.put("totalMarks", assessment.getTotalMarks() == null ? 0 : assessment.getTotalMarks());
        map.put("questionCount", assessment.getQuestions() == null ? 0 : assessment.getQuestions().size());
        map.put("accessLevel", assessment.getPublicAccessLevel());
        map.put("attemptLimit", assessment.getPublicAttemptLimit());

        return map;
    }

    private Map<String, Object> mapChallengeCard(PseudoCodeChallenge challenge) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", challenge.getId());
        map.put("type", "CHALLENGE");
        map.put("title", safe(challenge.getTitle()));
        map.put("description", safe(challenge.getProblemStatement()));
        map.put("company", challenge.getCompanyName() == null || challenge.getCompanyName().isBlank()
                ? "General"
                : challenge.getCompanyName());
        map.put("skill", challenge.getSkill() == null || challenge.getSkill().isBlank()
                ? "Coding"
                : challenge.getSkill());
        map.put("durationMinutes", challenge.getDurationMinutes() == null ? 0 : challenge.getDurationMinutes());
        map.put("totalMarks", challenge.getTotalMarks() == null ? 0 : challenge.getTotalMarks());
        map.put("questionCount", challenge.getTestCases() == null ? 0 : challenge.getTestCases().size());
        map.put("challengeGroupTitle", safe(challenge.getChallengeGroupTitle()));
        map.put("accessLevel", challenge.getPublicAccessLevel());
        map.put("attemptLimit", challenge.getPublicAttemptLimit());
        map.put("difficultyLevel", normalizedDifficulty(challenge));
        map.put("passPercentage", passPercentageForDifficulty(challenge));

        return map;
    }

    private Map<String, Object> mapInterviewQuestionCard(InterviewQuestion question) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", question.getId());
        map.put("type", "INTERVIEW");
        map.put("title", safe(question.getQuestion()));
        map.put("description", safe(question.getAnswer()));
        map.put("question", safe(question.getQuestion()));
        map.put("answer", safe(question.getAnswer()));
        map.put("company", question.getCompany() == null || question.getCompany().isBlank()
                ? "General"
                : question.getCompany());
        map.put("skill", question.getRole() == null || question.getRole().isBlank()
                ? "Interview Preparation"
                : question.getRole());
        map.put("role", question.getRole() == null || question.getRole().isBlank()
                ? "Interview Preparation"
                : question.getRole());
        map.put("topic", safe(question.getTopic()));
        map.put("difficulty", question.getDifficulty() == null || question.getDifficulty().isBlank()
                ? "MEDIUM"
                : question.getDifficulty());
        map.put("questionCount", 1);
        map.put("durationMinutes", 0);
        map.put("totalMarks", 0);
        map.put("accessLevel", question.getPublicAccessLevel());
        map.put("publishedAt", question.getPublishedAt());

        return map;
    }

    private boolean isRulePassed(PseudoCodeRule rule, String code) {
        String normalized = code == null ? "" : code.toLowerCase();

        return switch (String.valueOf(rule.getType())) {
            case "REQUIRED_KEYWORD" -> normalized.contains(String.valueOf(rule.getValue()).toLowerCase());
            case "FORBIDDEN_KEYWORD" -> !normalized.contains(String.valueOf(rule.getValue()).toLowerCase());
            case "MIN_LINES" -> code != null && code.split("\\R").length >= Integer.parseInt(rule.getValue());
            default -> false;
        };
    }

    private String detectCompany(String... values) {
        String text = String.join(" ", Arrays.stream(values)
                .filter(Objects::nonNull)
                .toList()
        ).toLowerCase();

        if (text.contains("tcs") || text.contains("nqt")) return "TCS";
        if (text.contains("deloitte") || text.contains("nla")) return "Deloitte";
        if (text.contains("infosys")) return "Infosys";
        if (text.contains("wipro")) return "Wipro";
        if (text.contains("accenture")) return "Accenture";
        if (text.contains("cognizant")) return "Cognizant";
        if (text.contains("zoho")) return "Zoho";

        return "General";
    }

    private String detectSkill(String... values) {
        String text = String.join(" ", Arrays.stream(values)
                .filter(Objects::nonNull)
                .toList()
        ).toLowerCase();

        if (text.contains("aptitude") || text.contains("quant")) return "Aptitude";
        if (text.contains("reasoning") || text.contains("logical")) return "Logical Reasoning";
        if (text.contains("verbal") || text.contains("communication")) return "Verbal Ability";
        if (text.contains("technical") || text.contains("programming")) return "Technical Basics";
        if (text.contains("pseudo") || text.contains("coding")) return "Coding";

        return "Placement Readiness";
    }

    @Transactional
    public Map<String, Object> savePracticeLead(Map<String, Object> payload) {
        Lead lead = buildLeadFromPayload(payload, "PUBLIC_PRACTICE_START");
        leadService.savePublicPracticeLead(lead);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("saved", true);
        response.put("message", "Lead saved successfully");
        response.put("phone", lead.getPhone());
        response.put("savedAt", LocalDateTime.now());

        return response;
    }

    @Transactional
    public Map<String, Object> registerPracticeAccess(Map<String, Object> payload) {
        String practiceType = String.valueOf(payload.getOrDefault("practiceType", "")).trim().toUpperCase();
        Long practiceId = Long.valueOf(String.valueOf(payload.get("practiceId")));

        if (!practiceType.equals("ASSESSMENT") && !practiceType.equals("CHALLENGE")) {
            throw new RuntimeException("Invalid practice type");
        }

        String accessLevel;
        int attemptLimit = PUBLIC_PRACTICE_ATTEMPT_LIMIT;

        if (practiceType.equals("ASSESSMENT")) {
            Assessment assessment = assessmentRepository.findById(practiceId)
                    .orElseThrow(() -> new RuntimeException("Assessment not found"));

            if (!Boolean.TRUE.equals(assessment.getActive()) || !Boolean.TRUE.equals(assessment.getPublicVisible())) {
                throw new RuntimeException("This assessment is not available publicly");
            }

            accessLevel = assessment.getPublicAccessLevel();
        } else {
            PseudoCodeChallenge challenge = challengeRepository.findById(practiceId)
                    .orElseThrow(() -> new RuntimeException("Challenge not found"));

            if (!Boolean.TRUE.equals(challenge.getActive()) || !Boolean.TRUE.equals(challenge.getPublicVisible())) {
                throw new RuntimeException("This challenge is not available publicly");
            }

            accessLevel = challenge.getPublicAccessLevel();
        }

        if (!"LEAD_REQUIRED".equalsIgnoreCase(accessLevel)) {
            throw new PracticeAccessRequiredException(
                    "This practice item is not open for guest registration. Please contact admin."
            );
        }

        Lead lead = saveOrReusePracticeLead(payload);

        PublicPracticeAccessGrant grant = PublicPracticeAccessGrant.builder()
                .leadId(lead.getId())
                .practiceType(practiceType)
                .practiceId(practiceId)
                .accessLevel(accessLevel)
                .accessToken(generateAccessToken())
                .maxAttempts(attemptLimit)
                .attemptsUsed(0)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        grantRepository.save(grant);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("accessToken", grant.getAccessToken());
        data.put("practiceType", grant.getPracticeType());
        data.put("practiceId", grant.getPracticeId());
        data.put("expiresAt", grant.getExpiresAt());
        data.put("maxAttempts", grant.getMaxAttempts());

        return data;
    }

    private Lead saveOrReusePracticeLead(Map<String, Object> payload) {
        Object rawLead = payload.get("lead");

        if (!(rawLead instanceof Map<?, ?> leadMap)) {
            throw new RuntimeException("Lead details are required");
        }

        Map<String, Object> leadPayload = new HashMap<>();
        leadPayload.put("lead", leadMap);

        Lead incoming = buildLeadFromPayload(leadPayload, "PUBLIC_PRACTICE_ACCESS");
        Optional<Lead> existing = leadRepository.findFirstByPhoneOrderByCreatedAtDesc(incoming.getPhone());

        if (existing.isPresent()) {
            Lead lead = existing.get();
            lead.setName(incoming.getName());
            lead.setEmail(incoming.getEmail());
            lead.setCity(incoming.getCity());
            lead.setCourse(incoming.getCourse());
            lead.setMessage(incoming.getMessage());
            lead.setSource("PUBLIC_PRACTICE_ACCESS");
            lead.setDeleted(false);
            return leadRepository.save(lead);
        }

        return leadRepository.save(incoming);
    }

    private String generateAccessToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private PublicPracticeAccessGrant requireValidGrant(
            String token,
            String practiceType,
            Long practiceId
    ) {
        if (token == null || token.isBlank()) {
            throw new PracticeAccessRequiredException("Please complete registration to continue");
        }

        PublicPracticeAccessGrant grant = grantRepository
                .findByAccessTokenAndActiveTrue(token)
                .orElseThrow(() ->
                        new PracticeAccessRequiredException("Please complete registration to continue"));

        if (!practiceType.equalsIgnoreCase(grant.getPracticeType())
                || !practiceId.equals(grant.getPracticeId())) {
            throw new PracticeAccessRequiredException("Invalid practice access");
        }

        if (grant.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new PracticeAccessRequiredException("Your practice access has expired");
        }

        if (grant.getAttemptsUsed() >= grant.getMaxAttempts()) {
            throw new PracticeAccessRequiredException("Attempt limit reached");
        }

        return grant;
    }

    private void markGrantUsed(PublicPracticeAccessGrant grant) {
        grant.setAttemptsUsed((grant.getAttemptsUsed() == null ? 0 : grant.getAttemptsUsed()) + 1);
        grantRepository.save(grant);
    }

    private void requireRegisteredLead(Map<String, Object> payload) {
        if (payload == null) {
            throw new PracticeAccessRequiredException(
                    "Please complete registration before continuing"
            );
        }

        Object rawLead = payload.get("lead");

        if (!(rawLead instanceof Map<?, ?> leadMap)) {
            throw new PracticeAccessRequiredException(
                    "Please complete registration before continuing"
            );
        }

        String phone = read(leadMap, "phone").replaceAll("\\D", "");

        if (phone.isBlank() || phone.length() < 10) {
            throw new PracticeAccessRequiredException(
                    "Please complete registration before continuing"
            );
        }

        if (!leadRepository.existsByPhoneAndDeletedFalse(phone)) {
            throw new PracticeAccessRequiredException(
                    "Please register before attempting this practice item"
            );
        }
    }
    private Lead buildLeadFromPayload(Map<String, Object> payload, String source) {
        if (payload == null) {
            throw new RuntimeException("Invalid lead request");
        }

        Map<?, ?> data;

        Object rawLead = payload.get("lead");

        if (rawLead instanceof Map<?, ?> leadMap) {
            data = leadMap;
        } else {
            data = payload;
        }

        String name = read(data, "name");
        String phone = read(data, "phone");
        String email = read(data, "email");
        String city = read(data, "city");
        String interest = read(data, "interest");
        String message = read(data, "message");

        phone = phone.replaceAll("\\D", "");

        if (name.isBlank()) {
            throw new RuntimeException("Name is required");
        }

        if (phone.isBlank()) {
            throw new RuntimeException("Phone is required");
        }

        if (phone.length() < 10) {
            throw new RuntimeException("Please enter a valid phone number");
        }

        if (phone.length() > 15) {
            phone = phone.substring(phone.length() - 15);
        }

        Lead lead = new Lead();
        lead.setName(name);
        lead.setPhone(phone);
        lead.setEmail(email);
        lead.setCity(city);
        lead.setCourse(interest.isBlank() ? "Placement Preparation" : interest);
        lead.setMessage(message);
        lead.setSource(source);
        lead.setDeleted(false);

        return lead;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getChallengeLeaderboard(Long challengeId) {
        PseudoCodeChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        List<Map<String, Object>> entries = buildLeaderboard(
                publicChallengeAttemptRepository.leaderboardByChallenge(challengeId),
                100
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("challengeId", challenge.getId());
        response.put("title", safe(challenge.getTitle()));
        response.put("company", challenge.getCompanyName() == null ? "General" : challenge.getCompanyName());
        response.put("totalMarks", challenge.getTotalMarks() == null ? 0 : challenge.getTotalMarks());
        response.put("entries", entries);
        response.put("topThree", entries.stream().limit(3).toList());

        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentDailyLeaderboard() {
        LocalDateTime start = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        return buildPeriodLeaderboard("DAILY", start, end);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentWeeklyLeaderboard() {
        LocalDateTime start = LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .with(LocalTime.MIN);

        LocalDateTime end = start.plusDays(7).minusSeconds(1);

        return buildPeriodLeaderboard("WEEKLY", start, end);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentMonthlyLeaderboard() {
        YearMonth month = YearMonth.now();

        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(LocalTime.MAX);

        return buildPeriodLeaderboard("MONTHLY", start, end);
    }

    private Map<String, Object> buildPeriodLeaderboard(
            String period,
            LocalDateTime start,
            LocalDateTime end
    ) {
        List<Map<String, Object>> entries = buildAggregateLeaderboard(
                publicChallengeAttemptRepository.leaderboardBetween(start, end),
                100
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("period", period);
        response.put("start", start);
        response.put("end", end);
        response.put("entries", entries);
        response.put("topThree", entries.stream().limit(3).toList());

        return response;
    }

    private List<Map<String, Object>> buildAggregateLeaderboard(
            List<PublicChallengeAttempt> attempts,
            int limit
    ) {
        Set<Long> challengeIds = new HashSet<>();
        for (PublicChallengeAttempt attempt : attempts) {
            if (attempt.getChallengeId() != null) {
                challengeIds.add(attempt.getChallengeId());
            }
        }
        Map<Long, PseudoCodeChallenge> challenges = new HashMap<>();
        if (!challengeIds.isEmpty()) {
            challengeRepository.findAllById(challengeIds)
                    .forEach(c -> challenges.put(c.getId(), c));
        }

        Map<Long, String> profileImageByUserId = new HashMap<>();
        Map<String, String> profileImageByEmail = new HashMap<>();
        preloadProfileImages(attempts, profileImageByUserId, profileImageByEmail);

        Map<String, Map<Long, PublicChallengeAttempt>> bestByParticipantChallenge = new LinkedHashMap<>();

        for (PublicChallengeAttempt attempt : attempts) {
            Long challengeId = attempt.getChallengeId();

            if (challengeId == null) {
                continue;
            }

            String participantKey = participantKey(attempt);

            bestByParticipantChallenge.putIfAbsent(participantKey, new LinkedHashMap<>());

            PublicChallengeAttempt existing = bestByParticipantChallenge
                    .get(participantKey)
                    .get(challengeId);

            if (existing == null || isBetterAttempt(attempt, existing)) {
                bestByParticipantChallenge.get(participantKey).put(challengeId, attempt);
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();

        for (Map<Long, PublicChallengeAttempt> participantAttempts : bestByParticipantChallenge.values()) {
            int attemptedChallenges = participantAttempts.size();
            int solvedChallenges = 0;
            int totalScore = 0;
            int totalPossibleMarks = 0;
            int percentageSum = 0;
            long totalExecutionTimeMs = 0L;

            PublicChallengeAttempt identityAttempt = participantAttempts.values()
                    .stream()
                    .findFirst()
                    .orElse(null);

            LocalDateTime latestSubmission = null;

            List<String> solvedChallengeTitles = new ArrayList<>();
            List<String> attemptedChallengeTitles = new ArrayList<>();
            List<Map<String, Object>> challengeBreakdown = new ArrayList<>();

            for (PublicChallengeAttempt attempt : participantAttempts.values()) {
                Long challengeId = attempt.getChallengeId();
                PseudoCodeChallenge challenge = challenges.get(challengeId);

                int percentage = attempt.getPercentage() == null ? 0 : attempt.getPercentage();
                int requiredPercentage = challenge == null ? 75 : passPercentageForDifficulty(challenge);
                boolean solved = percentage >= requiredPercentage;

                String difficultyLevel = challenge == null ? "MEDIUM" : normalizedDifficulty(challenge);
                String challengeTitle = challenge == null
                        ? "Challenge #" + challengeId
                        : safe(challenge.getTitle());

                attemptedChallengeTitles.add(challengeTitle);

                if (solved) {
                    solvedChallenges++;
                    solvedChallengeTitles.add(challengeTitle);
                }

                int score = attempt.getScore() == null ? 0 : attempt.getScore();
                int totalMarks = attempt.getTotalMarks() == null ? 0 : attempt.getTotalMarks();
                long executionTime = attempt.getTotalExecutionTimeMs() == null
                        ? 0L
                        : attempt.getTotalExecutionTimeMs();

                totalScore += score;
                totalPossibleMarks += totalMarks;
                percentageSum += percentage;
                totalExecutionTimeMs += executionTime;

                LocalDateTime submittedAt = attempt.getSubmittedAt();
                if (submittedAt != null && (latestSubmission == null || submittedAt.isAfter(latestSubmission))) {
                    latestSubmission = submittedAt;
                }

                Map<String, Object> breakdown = new LinkedHashMap<>();
                breakdown.put("challengeId", challengeId);
                breakdown.put("title", challengeTitle);
                breakdown.put("difficultyLevel", difficultyLevel);
                breakdown.put("requiredPercentage", requiredPercentage);
                breakdown.put("percentage", percentage);
                breakdown.put("score", score);
                breakdown.put("totalMarks", totalMarks);
                breakdown.put("status", solved ? "SOLVED" : "NOT_SOLVED");
                breakdown.put("solved", solved);
                breakdown.put("submittedAt", submittedAt);

                challengeBreakdown.add(breakdown);
            }

            int averagePercentage = attemptedChallenges == 0
                    ? 0
                    : Math.round((float) percentageSum / attemptedChallenges);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", maskName(identityAttempt == null ? "" : identityAttempt.getParticipantName()));
            row.put("email", maskEmail(identityAttempt == null ? "" : identityAttempt.getParticipantEmail()));
            row.put("phone", maskPhone(identityAttempt == null ? "" : identityAttempt.getParticipantPhone()));
            String profileImageUrl = profileImageUrlForAttempt(identityAttempt, profileImageByUserId, profileImageByEmail);

            row.put("profileImageUrl", profileImageUrl);
            row.put("userProfileImageUrl", profileImageUrl);
            row.put("authorProfileImageUrl", profileImageUrl);
            row.put("picture", profileImageUrl);
            row.put("attemptedChallenges", attemptedChallenges);
            row.put("solvedChallenges", solvedChallenges);
            row.put("solvedSummary", solvedChallenges + "/" + attemptedChallenges);
            row.put("score", totalScore);
            row.put("totalMarks", totalPossibleMarks);
            row.put("percentage", averagePercentage);
            row.put("totalExecutionTimeMs", totalExecutionTimeMs);
            row.put("submittedAt", latestSubmission);
            row.put("solvedChallengeTitles", solvedChallengeTitles);
            row.put("attemptedChallengeTitles", attemptedChallengeTitles);
            row.put("challengeBreakdown", challengeBreakdown);

            rows.add(row);
        }

        rows.sort((a, b) -> {
            int solvedCompare = Integer.compare(
                    ((Number) b.get("solvedChallenges")).intValue(),
                    ((Number) a.get("solvedChallenges")).intValue()
            );
            if (solvedCompare != 0) return solvedCompare;

            int scoreCompare = Integer.compare(
                    ((Number) b.get("score")).intValue(),
                    ((Number) a.get("score")).intValue()
            );
            if (scoreCompare != 0) return scoreCompare;

            int percentageCompare = Integer.compare(
                    ((Number) b.get("percentage")).intValue(),
                    ((Number) a.get("percentage")).intValue()
            );
            if (percentageCompare != 0) return percentageCompare;

            int timeCompare = Long.compare(
                    ((Number) a.get("totalExecutionTimeMs")).longValue(),
                    ((Number) b.get("totalExecutionTimeMs")).longValue()
            );
            if (timeCompare != 0) return timeCompare;

            LocalDateTime aSubmitted = (LocalDateTime) a.get("submittedAt");
            LocalDateTime bSubmitted = (LocalDateTime) b.get("submittedAt");

            if (aSubmitted == null && bSubmitted == null) return 0;
            if (aSubmitted == null) return 1;
            if (bSubmitted == null) return -1;

            return aSubmitted.compareTo(bSubmitted);
        });

        List<Map<String, Object>> ranked = new ArrayList<>();

        for (int i = 0; i < Math.min(rows.size(), limit); i++) {
            Map<String, Object> row = new LinkedHashMap<>(rows.get(i));
            row.put("rank", i + 1);
            ranked.add(row);
        }

        return ranked;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getContestAnnouncements() {
        return contestAnnouncementRepository.findTop10ByPublishedTrueOrderByCreatedAtDesc()
                .stream()
                .map(announcement -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", announcement.getId());
                    map.put("title", announcement.getTitle());
                    map.put("message", announcement.getMessage());
                    map.put("weekStart", announcement.getWeekStart());
                    map.put("weekEnd", announcement.getWeekEnd());
                    map.put("challengeId", announcement.getChallengeId());
                    map.put("winnersJson", announcement.getWinnersJson());
                    map.put("createdAt", announcement.getCreatedAt());
                    return map;
                })
                .toList();
    }

    @Transactional
    public Map<String, Object> publishWeeklyTopThreeAnnouncement() {
        LocalDateTime weekStart = LocalDateTime.now()
                .minusWeeks(1)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .with(LocalTime.MIN);

        LocalDateTime weekEnd = weekStart.plusDays(7).minusSeconds(1);

        List<Map<String, Object>> winners = buildAggregateLeaderboard(
                publicChallengeAttemptRepository.leaderboardBetween(weekStart, weekEnd),
                3
        );

        if (winners.isEmpty()) {
            return Map.of(
                    "published", false,
                    "message", "No contest attempts found for last week"
            );
        }

        String winnerNames = winners.stream()
                .map(w -> "#" + w.get("rank")
                        + " " + w.get("name")
                        + " - " + w.get("solvedChallenges") + " solved"
                        + ", " + w.get("score") + "/" + w.get("totalMarks") + " marks")
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String winnersJson;
        try {
            winnersJson = objectMapper.writeValueAsString(winners);
        } catch (Exception e) {
            winnersJson = "[]";
        }

        PublicContestAnnouncement announcement = PublicContestAnnouncement.builder()
                .title("Weekly Coding Contest Top 3 Winners")
                .message("Congratulations to this week's top performers: " + winnerNames)
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .challengeId(null)
                .winnersJson(winnersJson)
                .published(true)
                .createdAt(LocalDateTime.now())
                .build();

        contestAnnouncementRepository.save(announcement);

        notificationService.notifyAdmins(
                "Weekly contest winners announced",
                announcement.getMessage(),
                "WEEKLY_CONTEST_WINNERS",
                "/coding-contests"
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("published", true);
        response.put("announcementId", announcement.getId());
        response.put("winners", winners);

        return response;
    }

    private String profileImageUrlForAttempt(
            PublicChallengeAttempt attempt,
            Map<Long, String> profileImageByUserId,
            Map<String, String> profileImageByEmail
    ) {
        if (attempt == null) {
            return "";
        }

        if (attempt.getUserId() != null) {
            String url = profileImageByUserId.get(attempt.getUserId());
            if (url != null && !url.isBlank()) {
                return url;
            }
        }

        String email = attempt.getParticipantEmail();

        if (email == null || email.isBlank()) {
            return "";
        }

        String url = profileImageByEmail.get(email.trim().toLowerCase());
        return url == null ? "" : url;
    }

    private void preloadProfileImages(
            List<PublicChallengeAttempt> attempts,
            Map<Long, String> profileImageByUserId,
            Map<String, String> profileImageByEmail
    ) {
        Set<Long> userIds = new HashSet<>();
        Set<String> emails = new HashSet<>();

        for (PublicChallengeAttempt attempt : attempts) {
            if (attempt.getUserId() != null) {
                userIds.add(attempt.getUserId());
            }
            if (attempt.getParticipantEmail() != null && !attempt.getParticipantEmail().isBlank()) {
                emails.add(attempt.getParticipantEmail().trim().toLowerCase());
            }
        }

        if (!userIds.isEmpty()) {
            userRepository.findAllById(userIds).forEach(user -> {
                if (user.getProfileImageUrl() != null) {
                    profileImageByUserId.put(user.getId(), safeProfileImageUrl(user.getProfileImageUrl()));
                }
            });
        }

        if (!emails.isEmpty()) {
            userRepository.findByEmailIn(emails).forEach(user -> {
                if (user.getProfileImageUrl() != null && user.getEmail() != null) {
                    profileImageByEmail.put(user.getEmail().trim().toLowerCase(), safeProfileImageUrl(user.getProfileImageUrl()));
                }
            });
        }
    }
    private String safeProfileImageUrl(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String url = value.trim();

        if (url.length() > 1000) {
            return "";
        }

        return url.startsWith("https://") ? url : "";
    }

    private List<Map<String, Object>> buildLeaderboard(
            List<PublicChallengeAttempt> attempts,
            int limit
    ) {
        Map<String, PublicChallengeAttempt> bestByParticipant = new LinkedHashMap<>();

        for (PublicChallengeAttempt attempt : attempts) {
            String key = participantKey(attempt);
            PublicChallengeAttempt existing = bestByParticipant.get(key);

            if (existing == null || isBetterAttempt(attempt, existing)) {
                bestByParticipant.put(key, attempt);
            }
        }

        List<PublicChallengeAttempt> sorted = bestByParticipant.values()
                .stream()
                .sorted((a, b) -> {
                    int scoreCompare = Integer.compare(
                            b.getScore() == null ? 0 : b.getScore(),
                            a.getScore() == null ? 0 : a.getScore()
                    );

                    if (scoreCompare != 0) return scoreCompare;

                    int timeCompare = Long.compare(
                            a.getTotalExecutionTimeMs() == null ? Long.MAX_VALUE : a.getTotalExecutionTimeMs(),
                            b.getTotalExecutionTimeMs() == null ? Long.MAX_VALUE : b.getTotalExecutionTimeMs()
                    );

                    if (timeCompare != 0) return timeCompare;

                    return a.getSubmittedAt().compareTo(b.getSubmittedAt());
                })
                .limit(limit)
                .toList();

        Map<Long, String> profileImageByUserId = new HashMap<>();
        Map<String, String> profileImageByEmail = new HashMap<>();
        preloadProfileImages(sorted, profileImageByUserId, profileImageByEmail);

        List<Map<String, Object>> rows = new ArrayList<>();

        for (int i = 0; i < sorted.size(); i++) {
            PublicChallengeAttempt attempt = sorted.get(i);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank", i + 1);
            row.put("attemptId", attempt.getId());
            row.put("challengeId", attempt.getChallengeId());
            row.put("name", maskName(attempt.getParticipantName()));
            row.put("email", maskEmail(attempt.getParticipantEmail()));
            row.put("phone", maskPhone(attempt.getParticipantPhone()));
            row.put("score", attempt.getScore() == null ? 0 : attempt.getScore());
            row.put("totalMarks", attempt.getTotalMarks() == null ? 0 : attempt.getTotalMarks());
            row.put("percentage", attempt.getPercentage() == null ? 0 : attempt.getPercentage());
            row.put("status", attempt.getStatus());
            row.put("language", attempt.getLanguage());
            row.put("totalExecutionTimeMs", attempt.getTotalExecutionTimeMs() == null ? 0 : attempt.getTotalExecutionTimeMs());
            row.put("submittedAt", attempt.getSubmittedAt());
            String profileImageUrl = profileImageUrlForAttempt(attempt, profileImageByUserId, profileImageByEmail);

            row.put("profileImageUrl", profileImageUrl);
            row.put("userProfileImageUrl", profileImageUrl);
            row.put("authorProfileImageUrl", profileImageUrl);
            row.put("picture", profileImageUrl);

            rows.add(row);
        }

        return rows;
    }

    private Map<String, Object> getParticipantRankPreview(Long challengeId, PublicChallengeAttempt latestAttempt) {
        List<Map<String, Object>> leaderboard = buildLeaderboard(
                publicChallengeAttemptRepository.leaderboardByChallenge(challengeId),
                500
        );

        String currentKey = participantKey(latestAttempt);

        for (Map<String, Object> row : leaderboard) {
            Long attemptId = Long.valueOf(String.valueOf(row.get("attemptId")));
            if (attemptId.equals(latestAttempt.getId())) {
                return row;
            }
        }

        return Map.of(
                "score", latestAttempt.getScore() == null ? 0 : latestAttempt.getScore(),
                "message", "Attempt recorded"
        );
    }

    private boolean isBetterAttempt(PublicChallengeAttempt candidate, PublicChallengeAttempt existing) {
        int candidateScore = candidate.getScore() == null ? 0 : candidate.getScore();
        int existingScore = existing.getScore() == null ? 0 : existing.getScore();

        if (candidateScore != existingScore) {
            return candidateScore > existingScore;
        }

        int candidatePercentage = candidate.getPercentage() == null ? 0 : candidate.getPercentage();
        int existingPercentage = existing.getPercentage() == null ? 0 : existing.getPercentage();

        if (candidatePercentage != existingPercentage) {
            return candidatePercentage > existingPercentage;
        }

        long candidateTime = candidate.getTotalExecutionTimeMs() == null
                ? Long.MAX_VALUE
                : candidate.getTotalExecutionTimeMs();

        long existingTime = existing.getTotalExecutionTimeMs() == null
                ? Long.MAX_VALUE
                : existing.getTotalExecutionTimeMs();

        if (candidateTime != existingTime) {
            return candidateTime < existingTime;
        }

        if (candidate.getSubmittedAt() == null) return false;
        if (existing.getSubmittedAt() == null) return true;

        return candidate.getSubmittedAt().isBefore(existing.getSubmittedAt());
    }

    private int passPercentageForDifficulty(PseudoCodeChallenge challenge) {
        String difficulty = String.valueOf(
                challenge.getDifficultyLevel() == null ? "MEDIUM" : challenge.getDifficultyLevel()
        ).trim().toUpperCase();

        return switch (difficulty) {
            case "EASY" -> 60;
            case "HARD" -> 90;
            default -> 75;
        };
    }

    private String normalizedDifficulty(PseudoCodeChallenge challenge) {
        String difficulty = String.valueOf(
                challenge.getDifficultyLevel() == null ? "MEDIUM" : challenge.getDifficultyLevel()
        ).trim().toUpperCase();

        if (!Set.of("EASY", "MEDIUM", "HARD").contains(difficulty)) {
            return "MEDIUM";
        }

        return difficulty;
    }

    private String participantKey(PublicChallengeAttempt attempt) {
        if (attempt.getUserId() != null) {
            return "USER:" + attempt.getUserId();
        }

        if (attempt.getParticipantEmail() != null && !attempt.getParticipantEmail().isBlank()) {
            return "EMAIL:" + attempt.getParticipantEmail().trim().toLowerCase();
        }

        if (attempt.getParticipantPhone() != null && !attempt.getParticipantPhone().isBlank()) {
            return "PHONE:" + attempt.getParticipantPhone().replaceAll("\\D", "");
        }

        return "ATTEMPT:" + attempt.getId();
    }

    private String maskName(String name) {
        if (name == null || name.isBlank()) {
            return "Participant";
        }

        String trimmed = name.trim();

        if (trimmed.length() <= 2) {
            return trimmed.charAt(0) + "*";
        }

        int visible = Math.max(2, trimmed.length() / 2);
        String start = trimmed.substring(0, visible);

        return start + "*".repeat(trimmed.length() - visible);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return "";
        }

        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 2) {
            return local.charAt(0) + "***@" + domain;
        }

        int visible = Math.max(2, local.length() / 2);
        return local.substring(0, visible) + "*".repeat(local.length() - visible) + "@" + domain;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "";
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.length() <= 4) {
            return "*".repeat(digits.length());
        }

        String start = digits.substring(0, Math.min(2, digits.length()));
        String end = digits.substring(digits.length() - 2);

        return start + "*".repeat(Math.max(digits.length() - 4, 3)) + end;
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private String read(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    @Transactional
    public Map<String, Object> registerAuthenticatedPracticeAccess(Map<String, Object> payload) {
        Long userId = securityUtils.getCurrentUserId();

        if (userId == null) {
            throw new PracticeAccessRequiredException("Please login to continue");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PracticeAccessRequiredException("Please login to continue"));

        String practiceType = String.valueOf(payload.getOrDefault("practiceType", ""))
                .trim()
                .toUpperCase();

        Long practiceId = Long.valueOf(String.valueOf(payload.get("practiceId")));

        if (!practiceType.equals("ASSESSMENT")
                && !practiceType.equals("CHALLENGE")
                && !practiceType.equals("INTERVIEW")) {
            throw new RuntimeException("Invalid practice type");
        }

        String accessLevel;
        int attemptLimit = PUBLIC_PRACTICE_ATTEMPT_LIMIT;
        String requiredFeature;

        if (practiceType.equals("ASSESSMENT")) {
            Assessment assessment = assessmentRepository.findById(practiceId)
                    .orElseThrow(() -> new RuntimeException("Assessment not found"));

            if (!Boolean.TRUE.equals(assessment.getActive())
                    || !Boolean.TRUE.equals(assessment.getPublicVisible())) {
                throw new RuntimeException("This assessment is not available publicly");
            }

            accessLevel = assessment.getPublicAccessLevel();
            requiredFeature = "MOCK_TESTS";

        } else if (practiceType.equals("CHALLENGE")) {
            PseudoCodeChallenge challenge = challengeRepository.findById(practiceId)
                    .orElseThrow(() -> new RuntimeException("Challenge not found"));

            if (!Boolean.TRUE.equals(challenge.getActive())
                    || !Boolean.TRUE.equals(challenge.getPublicVisible())) {
                throw new RuntimeException("This challenge is not available publicly");
            }

            accessLevel = challenge.getPublicAccessLevel();
            requiredFeature = "PREMIUM_CHALLENGES";

        } else {
            InterviewQuestion question = interviewQuestionRepository.findById(practiceId)
                    .orElseThrow(() -> new RuntimeException("Interview question not found"));

            if (!Boolean.TRUE.equals(question.getActive())
                    || !Boolean.TRUE.equals(question.getPublicVisible())) {
                throw new RuntimeException("This interview practice item is not available publicly");
            }

            accessLevel = question.getPublicAccessLevel();
            requiredFeature = "INTERVIEWS";
        }

        enforceAuthenticatedAccessPolicy(
                accessLevel,
                requiredFeature,
                user.getId(),
                user.getEmail(),
                practiceType
        );

        Lead lead = saveOrReusePracticeLeadForUser(user, practiceType, practiceId);

        PublicPracticeAccessGrant grant = PublicPracticeAccessGrant.builder()
                .leadId(lead.getId())
                .practiceType(practiceType)
                .practiceId(practiceId)
                .accessLevel(accessLevel)
                .accessToken(generateAccessToken())
                .maxAttempts(attemptLimit)
                .attemptsUsed(0)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        grantRepository.save(grant);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("accessToken", grant.getAccessToken());
        data.put("practiceType", grant.getPracticeType());
        data.put("practiceId", grant.getPracticeId());
        data.put("expiresAt", grant.getExpiresAt());
        data.put("maxAttempts", grant.getMaxAttempts());
        data.put("authenticated", true);
        data.put("userId", user.getId());
        data.put("accessLevel", accessLevel);
        data.put("requiredFeature", requiredFeature);

        return data;
    }

    private void enforceAuthenticatedAccessPolicy(
            String accessLevel,
            String requiredFeature,
            Long userId,
            String email,
            String practiceType
    ) {
        String level = String.valueOf(accessLevel == null ? "LEAD_REQUIRED" : accessLevel)
                .trim()
                .toUpperCase();

        if (level.equals("PUBLIC_PREVIEW") || level.equals("LEAD_REQUIRED") || level.equals("PUBLIC")) {
            return;
        }

        if (level.equals("ACCOUNT_REQUIRED") || level.equals("ENROLLED_STUDENT_ONLY")) {
            return;
        }

        if (level.equals("BASIC_PLAN")) {
            if (planAccessService.hasTierAtLeast(userId, email, "BASIC")) return;
            throw new PracticeAccessRequiredException(premiumAccessMessage(requiredFeature, practiceType));
        }

        if (level.equals("PRO_PLAN")) {
            if (planAccessService.hasTierAtLeast(userId, email, "PRO")) return;
            throw new PracticeAccessRequiredException(premiumAccessMessage(requiredFeature, practiceType));
        }

        if (level.equals("ELITE_PLAN")) {
            if (planAccessService.hasTierAtLeast(userId, email, "ELITE")) return;
            throw new PracticeAccessRequiredException(premiumAccessMessage(requiredFeature, practiceType));
        }

        if (level.equals("PAID_STUDENT_ONLY")) {
            if (planAccessService.hasAnyActivePlan(userId, email)) return;
            throw new PracticeAccessRequiredException(premiumAccessMessage(requiredFeature, practiceType));
        }

        if (requiresPaidPlan(level)) {
            boolean allowed = planAccessService.hasFeatureAccess(userId, email, requiredFeature);

            if (!allowed && "PREMIUM_CHALLENGES".equals(requiredFeature)) {
                allowed = planAccessService.hasPremiumChallengeAccess(userId, email);
            }

            if (allowed) return;

            throw new PracticeAccessRequiredException(premiumAccessMessage(requiredFeature, practiceType));
        }

        throw new PracticeAccessRequiredException(
                "This practice item is not available for your account"
        );
    }

    private boolean requiresPaidPlan(String accessLevel) {
        String level = String.valueOf(accessLevel == null ? "" : accessLevel)
                .trim()
                .toUpperCase();

        return level.equals("PAID_STUDENT_ONLY")
                || level.equals("BASIC_PLAN")
                || level.equals("PRO_PLAN")
                || level.equals("ELITE_PLAN")
                || level.equals("PREMIUM")
                || level.equals("PRO_ONLY")
                || level.equals("ELITE_ONLY");
    }

    private String premiumAccessMessage(String requiredFeature, String practiceType) {
        return switch (requiredFeature) {
            case "MOCK_TESTS" ->
                    "Please purchase a plan with mock test access to unlock this assessment";
            case "PREMIUM_CHALLENGES" ->
                    "Please purchase a plan with premium coding challenge access to unlock this challenge";
            case "INTERVIEWS" ->
                    "Please purchase a plan with interview preparation access to unlock this practice item";
            case "VIDEOS" ->
                    "Please purchase a plan with video lessons access";
            case "LIVE_CLASSES" ->
                    "Please purchase a plan with live classes access";
            case "COURSES" ->
                    "Please purchase a plan with course access";
            case "NOTES", "MATERIALS" ->
                    "Please purchase a plan with notes and materials access";
            default ->
                    "Please purchase a premium plan to unlock this content";
        };
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getChallengeBestSubmissions(Long challengeId) {
        Long userId = securityUtils.getCurrentUserId();

        if (userId == null) {
            throw new PracticeAccessRequiredException("Please purchase any plan to unlock best answers");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PracticeAccessRequiredException("Please purchase any plan to unlock best answers"));

        if (!planAccessService.hasAnyActivePlan(user.getId(), user.getEmail())) {
            throw new PracticeAccessRequiredException("Please purchase any plan to unlock best answers");
        }

        PseudoCodeChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        List<PublicChallengeAttempt> attempts =
                publicChallengeAttemptRepository.bestAnswerSubmissions(challengeId);

        Map<String, PublicChallengeAttempt> bestByParticipant = new LinkedHashMap<>();

        for (PublicChallengeAttempt attempt : attempts) {
            String key = participantKey(attempt);
            PublicChallengeAttempt existing = bestByParticipant.get(key);

            if (existing == null || isBetterAttempt(attempt, existing)) {
                bestByParticipant.put(key, attempt);
            }
        }

        List<PublicChallengeAttempt> sorted = bestByParticipant.values()
                .stream()
                .sorted((a, b) -> {
                    int percentageCompare = Integer.compare(
                            b.getPercentage() == null ? 0 : b.getPercentage(),
                            a.getPercentage() == null ? 0 : a.getPercentage()
                    );
                    if (percentageCompare != 0) return percentageCompare;

                    int scoreCompare = Integer.compare(
                            b.getScore() == null ? 0 : b.getScore(),
                            a.getScore() == null ? 0 : a.getScore()
                    );
                    if (scoreCompare != 0) return scoreCompare;

                    return Long.compare(
                            a.getTotalExecutionTimeMs() == null ? Long.MAX_VALUE : a.getTotalExecutionTimeMs(),
                            b.getTotalExecutionTimeMs() == null ? Long.MAX_VALUE : b.getTotalExecutionTimeMs()
                    );
                })
                .limit(30)
                .toList();

        List<Map<String, Object>> rows = new ArrayList<>();

        for (int i = 0; i < sorted.size(); i++) {
            PublicChallengeAttempt attempt = sorted.get(i);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank", i + 1);
            row.put("attemptId", attempt.getId());
            row.put("challengeId", attempt.getChallengeId());
            row.put("name", maskName(attempt.getParticipantName()));
            row.put("email", maskEmail(attempt.getParticipantEmail()));
            row.put("score", attempt.getScore() == null ? 0 : attempt.getScore());
            row.put("totalMarks", attempt.getTotalMarks() == null ? 0 : attempt.getTotalMarks());
            row.put("percentage", attempt.getPercentage() == null ? 0 : attempt.getPercentage());
            row.put("status", attempt.getStatus());
            row.put("language", attempt.getLanguage());
            row.put("sourceCode", attempt.getSourceCode() == null ? "" : attempt.getSourceCode());
            row.put("totalExecutionTimeMs", attempt.getTotalExecutionTimeMs() == null ? 0 : attempt.getTotalExecutionTimeMs());
            row.put("submittedAt", attempt.getSubmittedAt());

            rows.add(row);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("challengeId", challenge.getId());
        response.put("title", safe(challenge.getTitle()));
        response.put("totalMarks", challenge.getTotalMarks() == null ? 0 : challenge.getTotalMarks());
        response.put("minimumPercentage", 80);
        response.put("entries", rows);

        return response;
    }

    private boolean hasTierAtLeast(UserPlanAccessDto access, String requiredTier) {
        if (access == null || !access.isActive()) return false;

        int userRank = tierRank(access.getTier());
        int requiredRank = tierRank(requiredTier);

        return userRank >= requiredRank;
    }

    private int tierRank(String tier) {
        if (tier == null) return 0;

        return switch (tier.trim().toUpperCase()) {
            case "BASIC", "STARTER" -> 1;
            case "PRO" -> 2;
            case "ELITE" -> 3;
            default -> 0;
        };
    }

    private Lead saveOrReusePracticeLeadForUser(User user, String practiceType, Long practiceId) {
        String phone = user.getPhone() == null ? "" : user.getPhone().replaceAll("\\D", "");

        if (phone.isBlank()) {
            phone = "USER" + user.getId();
        }

        Optional<Lead> existing = leadRepository.findFirstByPhoneOrderByCreatedAtDesc(phone);

        Lead lead = existing.orElseGet(Lead::new);
        lead.setName(user.getName() == null || user.getName().isBlank() ? "Student" : user.getName());
        lead.setEmail(user.getEmail());
        lead.setPhone(phone);
        lead.setCourse("Coding Contest");
        lead.setMessage("Authenticated user access for " + practiceType + " #" + practiceId);
        lead.setSource("PUBLIC_CONTEST_AUTHENTICATED");
        lead.setDeleted(false);

        return leadRepository.save(lead);
    }
}