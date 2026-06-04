package com.vidhuratech.jobs.student.service;

import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.batch.entity.BatchEnrollment;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.trainer.entity.*;
import com.vidhuratech.jobs.trainer.repository.PseudoCodeAttemptRepository;
import com.vidhuratech.jobs.trainer.repository.PseudoCodeChallengeRepository;
import com.vidhuratech.jobs.trainer.repository.PseudoCodeDraftRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentPseudoCodeChallengeService {

    private final PseudoCodeChallengeRepository challengeRepository;
    private final PseudoCodeAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final BatchEnrollmentRepository batchEnrollmentRepository;
    private final SecurityUtils securityUtils;
    private final CodeExecutionService codeExecutionService;
    private final PseudoCodeDraftRepository draftRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudentChallenges() {
        User student = getCurrentStudent();

        List<Long> batchIds = batchEnrollmentRepository.findActiveByStudentEmail(student.getEmail())
                .stream()
                .map(BatchEnrollment::getBatch)
                .map(batch -> batch.getId())
                .toList();

        if (batchIds.isEmpty()) {
            return new ArrayList<>();
        }

        return challengeRepository.findByBatchIdInAndActiveTrueOrderByCreatedAtDesc(batchIds)
                .stream()
                .map(challenge -> mapChallengeForStudent(challenge, student))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getChallenge(Long id) {

        User student = getCurrentStudent();

        PseudoCodeChallenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        verifyStudentHasAccess(student, challenge.getBatchId());

        Map<String, Object> map =
                mapChallengeForStudent(challenge, student);

        map.put("constraintsText", challenge.getConstraintsText());
        map.put("inputFormat", challenge.getInputFormat());
        map.put("outputFormat", challenge.getOutputFormat());
        map.put("hintText", challenge.getHintText());

        map.put(
                "testCases",
                challenge.getTestCases().stream().map(tc -> {

                    Map<String, Object> t = new LinkedHashMap<>();

                    t.put("id", tc.getId());
                    t.put("inputData", tc.getInputData());
                    t.put("marks", tc.getMarks());
                    t.put("expectedOutput", tc.getExpectedOutput());
                    t.put("hidden", Boolean.TRUE.equals(tc.getHidden()));
                    return t;

                }).toList()
        );

        Map<String, String> savedDrafts =
                new LinkedHashMap<>();

        for (String lang : List.of(
                "PYTHON",
                "JAVA",
                "C",
                "CPP",
                "CSHARP",
                "FSHARP",
                "PHP",
                "RUBY",
                "HASKELL",
                "GO",
                "RUST",
                "TYPESCRIPT"
        )) {

            draftRepository
                    .findTopByChallengeIdAndStudentIdAndLanguageOrderBySavedAtDesc(
                            challenge.getId(),
                            student.getId(),
                            lang
                    )
                    .ifPresent(draft ->
                            savedDrafts.put(
                                    lang,
                                    draft.getSourceCode()
                            )
                    );
        }

        map.put(
                "savedDrafts",
                savedDrafts
        );

        return map;
    }

    @Transactional
    public Map<String, Object> submitChallenge(Long challengeId, Map<String, Object> payload) {
        User student = getCurrentStudent();

        PseudoCodeChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        verifyStudentHasAccess(student, challenge.getBatchId());

        String language = String.valueOf(payload.getOrDefault("language", "")).trim().toUpperCase();
        String sourceCode = String.valueOf(payload.getOrDefault("sourceCode", ""));

        if (!Set.of(
                "JAVA",
                "PYTHON",
                "C",
                "CPP",
                "CSHARP",
                "FSHARP",
                "PHP",
                "RUBY",
                "HASKELL",
                "GO",
                "RUST",
                "TYPESCRIPT"
        ).contains(language)) {
            throw new RuntimeException("Unsupported language");
        }

        if (sourceCode.trim().isEmpty()) {
            throw new RuntimeException("Source code is required");
        }

        int score = 0;
        int total = challenge.getTotalMarks() == null ? 0 : challenge.getTotalMarks();
        boolean allTestsPassed = true;
        String compileError = "";

        PseudoCodeAttempt attempt = PseudoCodeAttempt.builder()
                .challenge(challenge)
                .student(student)
                .language(language)
                .sourceCode(sourceCode)
                .score(0)
                .totalMarks(total)
                .percentage(0)
                .status("FAIL")
                .allTestsPassed(false)
                .submittedAt(LocalDateTime.now())
                .build();

        attempt = attemptRepository.save(attempt);

        for (PseudoCodeRule rule : challenge.getRules()) {
            if (isRulePassed(rule, sourceCode)) {
                score += rule.getMarks() == null ? 0 : rule.getMarks();
            }
        }

        for (PseudoCodeTestCase testCase : challenge.getTestCases()) {
            CodeExecutionService.ExecutionResult execution =
                    codeExecutionService.run(language, sourceCode, testCase.getInputData());

            boolean correct = execution.isSuccess()
                    && normalize(execution.getOutput()).equals(normalize(testCase.getExpectedOutput()));

            if (!correct) {
                allTestsPassed = false;
            }

            int marks = correct ? (testCase.getMarks() == null ? 0 : testCase.getMarks()) : 0;
            score += marks;

            if (!execution.isSuccess() && compileError.isBlank()) {
                compileError = execution.getError();
            }

            attempt.getOutputs().add(
                    PseudoCodeAttemptOutput.builder()
                            .attempt(attempt)
                            .testCaseId(testCase.getId())
                            .inputData(testCase.getInputData())
                            .expectedOutput(testCase.getExpectedOutput())
                            .actualOutput(execution.getOutput())
                            .errorMessage(execution.getError())
                            .correct(correct)
                            .marksObtained(marks)
                            .executionTimeMs(execution.getExecutionTimeMs())
                            .build()
            );
        }

        int percentage = total == 0 ? 0 : (score * 100) / total;
        int passPercentage = challenge.getPassPercentage() == null ? 100 : challenge.getPassPercentage();

        attempt.setScore(score);
        attempt.setPercentage(percentage);
        attempt.setStatus(percentage >= passPercentage ? "PASS" : "FAIL");
        attempt.setAllTestsPassed(allTestsPassed);
        attempt.setCompileError(compileError);

        attemptRepository.save(attempt);

        return mapAttemptResult(attempt);
    }

    @Transactional
    public Map<String, Object> saveDraft(Long challengeId, Map<String, Object> payload) {

        User student = getCurrentStudent();

        PseudoCodeChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        String language = String.valueOf(payload.get("language"));
        String sourceCode = String.valueOf(payload.get("sourceCode"));

        PseudoCodeDraft draft = PseudoCodeDraft.builder()
                .challenge(challenge)
                .student(student)
                .language(language)
                .sourceCode(sourceCode)
                .savedAt(LocalDateTime.now())
                .build();

        draftRepository.save(draft);

        return Map.of(
                "saved", true,
                "savedAt", draft.getSavedAt()
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> runChallenge(Long challengeId, Map<String, Object> payload) {
        User student = getCurrentStudent();

        PseudoCodeChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        verifyStudentHasAccess(student, challenge.getBatchId());

        String language = String.valueOf(payload.get("language")).trim().toUpperCase();
        String sourceCode = String.valueOf(payload.get("sourceCode"));

        List<Map<String, Object>> results = new ArrayList<>();

        int score = 0;
        String compileError = "";

        for (PseudoCodeTestCase testCase : challenge.getTestCases()) {
            CodeExecutionService.ExecutionResult execution =
                    codeExecutionService.run(language, sourceCode, testCase.getInputData());

            boolean correct =
                    execution.isSuccess() &&
                            normalize(execution.getOutput()).equals(normalize(testCase.getExpectedOutput()));

            int marks = correct ? (testCase.getMarks() == null ? 0 : testCase.getMarks()) : 0;
            score += marks;

            if (!execution.isSuccess() && compileError.isBlank()) {
                compileError = execution.getError();
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("inputData", testCase.getInputData());
            item.put("expectedOutput", testCase.getExpectedOutput());
            item.put("actualOutput", execution.getOutput());
            item.put("status", correct ? "PASS" : "FAIL");
            item.put("errorMessage", execution.getError());
            item.put("marks", testCase.getMarks());
            item.put("marksObtained", marks);
            item.put("executionTimeMs", execution.getExecutionTimeMs());

            results.add(item);
        }

        int totalMarks = challenge.getTotalMarks() == null ? 0 : challenge.getTotalMarks();
        int passPercentage = challenge.getPassPercentage() == null ? 100 : challenge.getPassPercentage();
        int percentage = totalMarks == 0 ? 0 : (score * 100) / totalMarks;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", percentage >= passPercentage ? "PASS" : "FAIL");
        response.put("percentage", percentage);
        response.put("compileError", compileError);
        response.put("testResults", results);

        return response;
    }

    private Map<String, Object> mapAttemptResult(PseudoCodeAttempt attempt) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("attemptId", attempt.getId());
        map.put("challengeId", attempt.getChallenge().getId());
        map.put("language", attempt.getLanguage());
        map.put("score", attempt.getScore());
        map.put("totalMarks", attempt.getTotalMarks());
        map.put("percentage", attempt.getPercentage());
        map.put("status", attempt.getStatus());
        map.put("allTestsPassed", attempt.getAllTestsPassed());
        map.put("compileError", attempt.getCompileError());
        map.put("submittedAt", attempt.getSubmittedAt());

        map.put("testResults", attempt.getOutputs().stream().map(output -> {
            Map<String, Object> item = new LinkedHashMap<>();

            item.put("testCaseId", output.getTestCaseId());
            item.put("status", Boolean.TRUE.equals(output.getCorrect()) ? "PASS" : "FAIL");
            item.put("inputData", output.getInputData());
            item.put("expectedOutput", output.getExpectedOutput());
            item.put("actualOutput", output.getActualOutput());
            item.put("errorMessage", output.getErrorMessage());
            item.put("marks", output.getMarksObtained());
            item.put("marksObtained", output.getMarksObtained());
            item.put("executionTimeMs", output.getExecutionTimeMs());

            return item;
        }).toList());

        return map;
    }

    private User getCurrentStudent() {
        return userRepository.findByEmail(securityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    private void verifyStudentHasAccess(User student, Long batchId) {
        boolean enrolled = batchEnrollmentRepository.findActiveByStudentEmail(student.getEmail())
                .stream()
                .anyMatch(enrollment -> enrollment.getBatch().getId().equals(batchId));

        if (!enrolled) {
            throw new RuntimeException("Access denied");
        }
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

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private Map<String, Object> mapChallengeForStudent(PseudoCodeChallenge challenge, User student) {
        List<PseudoCodeAttempt> attempts =
                attemptRepository.findByChallengeIdAndStudentIdOrderBySubmittedAtDesc(
                        challenge.getId(),
                        student.getId()
                );

        PseudoCodeAttempt latest = attempts.isEmpty() ? null : attempts.get(0);

        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", challenge.getId());
        map.put("batchId", challenge.getBatchId());
        map.put("title", challenge.getTitle());
        map.put("problemStatement", challenge.getProblemStatement());
        map.put("durationMinutes", challenge.getDurationMinutes());
        map.put("totalMarks", challenge.getTotalMarks());
        map.put("passPercentage", challenge.getPassPercentage());
        map.put("skill", challenge.getSkill() == null ? "" : challenge.getSkill());
        map.put("difficultyLevel", challenge.getDifficultyLevel() == null ? "MEDIUM" : challenge.getDifficultyLevel());
        map.put("createdAt", challenge.getCreatedAt());
        map.put("rulesCount", challenge.getRules() == null ? 0 : challenge.getRules().size());
        map.put("testCasesCount", challenge.getTestCases() == null ? 0 : challenge.getTestCases().size());
        map.put("attemptCount", attempts.size());
        map.put("lastScore", latest == null ? 0 : latest.getScore());
        map.put("percentage", latest == null ? 0 : latest.getPercentage());
        map.put("status", latest == null ? "NOT_ATTEMPTED" : latest.getStatus());
        map.put("language", latest == null ? null : latest.getLanguage());
        map.put("allTestsPassed", latest != null && Boolean.TRUE.equals(latest.getAllTestsPassed()));
        map.put("lastSubmittedAt", latest == null ? null : latest.getSubmittedAt());
        map.put("challengeGroupId", challenge.getChallengeGroupId() == null ? "LEGACY-" + challenge.getId() : challenge.getChallengeGroupId());
        map.put("challengeGroupTitle", challenge.getChallengeGroupTitle() == null ? challenge.getTitle() : challenge.getChallengeGroupTitle());
        map.put("companyName",challenge.getCompanyName() == null ? "" : challenge.getCompanyName());

        return map;
    }
}