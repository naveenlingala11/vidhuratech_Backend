package com.vidhuratech.jobs.publicpractice.service;

import com.vidhuratech.jobs.common.exception.PracticeAccessRequiredException;
import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.leads.entity.Lead;
import com.vidhuratech.jobs.leads.repository.LeadRepository;
import com.vidhuratech.jobs.leads.service.LeadService;
import com.vidhuratech.jobs.student.service.CodeExecutionService;
import com.vidhuratech.jobs.trainer.entity.*;
import com.vidhuratech.jobs.trainer.repository.AssessmentRepository;
import com.vidhuratech.jobs.trainer.repository.PseudoCodeChallengeRepository;
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
import java.util.Base64;

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

    private final PublicPracticeAccessGrantRepository grantRepository;
    private final PublicAssessmentAttemptRepository publicAssessmentAttemptRepository;
    private final PublicChallengeAttemptRepository publicChallengeAttemptRepository;
    private final AssessmentRepository assessmentRepository;
    private final PseudoCodeChallengeRepository challengeRepository;
    private final LeadService leadService;
    private final CodeExecutionService codeExecutionService;
    private final LeadRepository leadRepository;
    private final ActivityNotificationService notificationService;
    private final UserRepository userRepository;

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

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("assessments", assessments);
        response.put("challenges", challenges);
        response.put("companies", companies);
        response.put("skills", skills);
        response.put("totalItems", assessments.size() + challenges.size());

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
                        .filter(item -> company.equalsIgnoreCase(
                                String.valueOf(item.get("company"))
                        ))
                        .toList();

        List<Map<String, Object>> challenges =
                ((List<Map<String, Object>>) library.get("challenges"))
                        .stream()
                        .filter(item -> company.equalsIgnoreCase(
                                String.valueOf(item.get("company"))
                        ))
                        .toList();

        Set<String> skills = new TreeSet<>();

        assessments.forEach(item -> skills.add(String.valueOf(item.get("skill"))));
        challenges.forEach(item -> skills.add(String.valueOf(item.get("skill"))));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("company", company);
        response.put("assessments", assessments);
        response.put("challenges", challenges);
        response.put("skills", skills);
        response.put("totalItems", assessments.size() + challenges.size());

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

        if (sourceCode.trim().isEmpty()) {
            throw new RuntimeException("Source code is required");
        }

        int score = 0;
        List<Map<String, Object>> results = new ArrayList<>();

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
        }

        int totalMarks = challenge.getTotalMarks() == null ? 0 : challenge.getTotalMarks();
        int percentage = totalMarks == 0 ? 0 : Math.round((score * 100f) / totalMarks);
        int passPercentage = challenge.getPassPercentage() == null ? 40 : challenge.getPassPercentage();
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
        response.put("submittedAt", LocalDateTime.now());
        response.put("testResults", results);
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
        int attemptLimit;

        if (practiceType.equals("ASSESSMENT")) {
            Assessment assessment = assessmentRepository.findById(practiceId)
                    .orElseThrow(() -> new RuntimeException("Assessment not found"));

            if (!Boolean.TRUE.equals(assessment.getActive()) || !Boolean.TRUE.equals(assessment.getPublicVisible())) {
                throw new RuntimeException("This assessment is not available publicly");
            }

            accessLevel = assessment.getPublicAccessLevel();
            attemptLimit = assessment.getPublicAttemptLimit() == null ? 1 : assessment.getPublicAttemptLimit();
        } else {
            PseudoCodeChallenge challenge = challengeRepository.findById(practiceId)
                    .orElseThrow(() -> new RuntimeException("Challenge not found"));

            if (!Boolean.TRUE.equals(challenge.getActive()) || !Boolean.TRUE.equals(challenge.getPublicVisible())) {
                throw new RuntimeException("This challenge is not available publicly");
            }

            accessLevel = challenge.getPublicAccessLevel();
            attemptLimit = challenge.getPublicAttemptLimit() == null ? 1 : challenge.getPublicAttemptLimit();
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
}