package com.vidhuratech.jobs.trainer.service;

import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;
import com.vidhuratech.jobs.trainer.entity.*;
import com.vidhuratech.jobs.trainer.repository.PseudoCodeAttemptRepository;
import com.vidhuratech.jobs.trainer.repository.PseudoCodeChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TrainerPseudoCodeChallengeService {

    private final PseudoCodeChallengeRepository challengeRepository;
    private final PseudoCodeAttemptRepository attemptRepository;
    private final BatchRepository batchRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public Map<String, Object> createChallenge(Map<String, Object> payload) {
        return createChallenge(payload, null, null, null);
    }

    private Map<String, Object> createChallenge(
            Map<String, Object> payload,
            String forcedGroupId,
            String forcedGroupTitle,
            String forcedCompanyName
    ) {
        String email = securityUtils.getCurrentUserEmail();

        Long batchId = Long.valueOf(String.valueOf(payload.get("batchId")));

        batchRepository.findByIdAndTrainerEmail(batchId, email)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        String title = String.valueOf(payload.get("title"));

        String groupId = forcedGroupId != null
                ? forcedGroupId
                : String.valueOf(payload.getOrDefault("challengeGroupId", UUID.randomUUID().toString()));

        String groupTitle = forcedGroupTitle != null
                ? forcedGroupTitle
                : String.valueOf(payload.getOrDefault("challengeGroupTitle", title));

        String companyName = forcedCompanyName != null
                ? forcedCompanyName
                : String.valueOf(payload.getOrDefault("companyName", ""));

        PseudoCodeChallenge challenge = PseudoCodeChallenge.builder()
                .batchId(batchId)
                .title(title)
                .problemStatement(String.valueOf(payload.get("problemStatement")))
                .constraintsText(String.valueOf(payload.getOrDefault("constraintsText", "")))
                .inputFormat(String.valueOf(payload.getOrDefault("inputFormat", "")))
                .outputFormat(String.valueOf(payload.getOrDefault("outputFormat", "")))
                .durationMinutes(Integer.valueOf(String.valueOf(payload.getOrDefault("durationMinutes", 30))))
                .totalMarks(Integer.valueOf(String.valueOf(payload.getOrDefault("totalMarks", 100))))
                .passPercentage(Integer.valueOf(String.valueOf(payload.getOrDefault("passPercentage", 40))))
                .trainerEmail(email)
                .active(true)
                .createdAt(LocalDateTime.now())
                .challengeGroupId(groupId)
                .challengeGroupTitle(groupTitle)
                .companyName(companyName)
                .build();

        Object rawRules = payload.getOrDefault("rules", List.of());

        if (rawRules instanceof List<?> rules) {
            for (Object obj : rules) {
                if (!(obj instanceof Map<?, ?> r)) continue;

                challenge.getRules().add(
                        PseudoCodeRule.builder()
                                .challenge(challenge)
                                .type(String.valueOf(r.get("type")))
                                .value(String.valueOf(r.get("value")))
                                .marks(getInt(r, "marks", 0))
                                .build()
                );
            }
        }

        Object rawTestCases = payload.getOrDefault("testCases", List.of());

        if (rawTestCases instanceof List<?> testCases) {
            int index = 0;

            for (Object obj : testCases) {
                if (!(obj instanceof Map<?, ?> tc)) continue;

                challenge.getTestCases().add(
                        PseudoCodeTestCase.builder()
                                .challenge(challenge)
                                .inputData(getString(tc, "inputData", ""))
                                .expectedOutput(getString(tc, "expectedOutput", ""))
                                .marks(getInt(tc, "marks", 0))
                                .hidden(index >= 3)
                                .build()
                );

                index++;
            }
        }

        PseudoCodeChallenge saved = challengeRepository.save(challenge);

        return Map.of(
                "challengeId", saved.getId(),
                "challengeGroupId", saved.getChallengeGroupId(),
                "challengeGroupTitle", saved.getChallengeGroupTitle(),
                "companyName", saved.getCompanyName() == null ? "" : saved.getCompanyName(),
                "title", saved.getTitle(),
                "rulesCount", saved.getRules().size(),
                "testCasesCount", saved.getTestCases().size(),
                "message", "Pseudo code challenge created successfully"
        );
    }

     @Transactional
    public Map<String, Object> updateChallenge(Long id, Map<String, Object> payload) {

        String email = securityUtils.getCurrentUserEmail();

        PseudoCodeChallenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        if (!email.equals(challenge.getTrainerEmail())) {
            throw new RuntimeException("Access denied");
        }

        Long batchId = Long.valueOf(String.valueOf(payload.get("batchId")));

        batchRepository.findByIdAndTrainerEmail(batchId, email)
                .orElseThrow(() -> new RuntimeException("Access denied for batch"));

        challenge.setBatchId(batchId);
        challenge.setTitle(String.valueOf(payload.get("title")));
        challenge.setProblemStatement(String.valueOf(payload.get("problemStatement")));
        challenge.setConstraintsText(
                String.valueOf(payload.getOrDefault("constraintsText", ""))
        );
        challenge.setInputFormat(
                String.valueOf(payload.getOrDefault("inputFormat", ""))
        );
        challenge.setOutputFormat(
                String.valueOf(payload.getOrDefault("outputFormat", ""))
        );
        challenge.setDurationMinutes(
                Integer.valueOf(String.valueOf(
                        payload.getOrDefault("durationMinutes", 30)
                ))
        );
        challenge.setTotalMarks(
                Integer.valueOf(String.valueOf(
                        payload.getOrDefault("totalMarks", 100)
                ))
        );
        challenge.setPassPercentage(
                Integer.valueOf(String.valueOf(
                        payload.getOrDefault("passPercentage", 40)
                ))
        );

        // clear old rules
        challenge.getRules().clear();

        Object rawRules = payload.getOrDefault("rules", List.of());

        if (rawRules instanceof List<?> rules) {

            for (Object obj : rules) {

                if (!(obj instanceof Map<?, ?> r)) continue;

                challenge.getRules().add(
                        PseudoCodeRule.builder()
                                .challenge(challenge)
                                .type(String.valueOf(r.get("type")))
                                .value(String.valueOf(r.get("value")))
                                .marks(getInt(r, "marks", 0))
                                .build()
                );
            }
        }

        // clear old test cases
        challenge.getTestCases().clear();

        Object rawTestCases = payload.getOrDefault("testCases", List.of());

        if (rawTestCases instanceof List<?> testCases) {
            int index = 0;
            for (Object obj : testCases) {

                if (!(obj instanceof Map<?, ?> tc)) continue;

                challenge.getTestCases().add(
                        PseudoCodeTestCase.builder()
                                .challenge(challenge)
                                .inputData(getString(tc, "inputData", ""))
                                .expectedOutput(getString(tc, "expectedOutput", ""))
                                .marks(getInt(tc, "marks", 0))
                                .hidden(index >= 3)
                                .build()
                );
                index++;
            }
        }

        PseudoCodeChallenge updated = challengeRepository.save(challenge);

        return Map.of(
                "challengeId", updated.getId(),
                "title", updated.getTitle(),
                "rulesCount", updated.getRules().size(),
                "testCasesCount", updated.getTestCases().size(),
                "message", "Pseudo code challenge updated successfully"
        );
    }

    @Transactional
    public Map<String, Object> createBulkChallenges(List<Map<String, Object>> payloads) {
        List<Map<String, Object>> created = new ArrayList<>();

        int successCount = 0;
        int failedCount = 0;

        String groupId = UUID.randomUUID().toString();

        String groupTitle = payloads.isEmpty()
                ? "Challenge Group"
                : String.valueOf(payloads.get(0).getOrDefault(
                "challengeGroupTitle",
                payloads.get(0).getOrDefault("groupTitle", "Challenge Group")
        ));

        String companyName = payloads.isEmpty()
                ? ""
                : String.valueOf(payloads.get(0).getOrDefault("companyName", ""));

        for (Map<String, Object> payload : payloads) {
            try {
                Map<String, Object> saved = createChallenge(payload, groupId, groupTitle, companyName);
                created.add(saved);
                successCount++;
            } catch (Exception ex) {
                failedCount++;
                created.add(
                        Map.of(
                                "title", String.valueOf(payload.getOrDefault("title", "Unknown")),
                                "error", ex.getMessage()
                        )
                );
            }
        }

        return Map.of(
                "challengeGroupId", groupId,
                "challengeGroupTitle", groupTitle,
                "companyName", companyName,
                "successCount", successCount,
                "failedCount", failedCount,
                "results", created
        );
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTrainerChallenges() {
        String email = securityUtils.getCurrentUserEmail();

        return challengeRepository.findByTrainerEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::mapChallengeListItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getChallengeDetails(Long id) {
        String email = securityUtils.getCurrentUserEmail();

        PseudoCodeChallenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        if (!email.equals(challenge.getTrainerEmail())) {
            throw new RuntimeException("Access denied");
        }

        return mapChallengeDetails(challenge);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAttempts(Long challengeId) {
        String email = securityUtils.getCurrentUserEmail();

        PseudoCodeChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        if (!email.equals(challenge.getTrainerEmail())) {
            throw new RuntimeException("Access denied");
        }

        return attemptRepository.findByChallengeIdOrderBySubmittedAtDesc(challengeId)
                .stream()
                .map(attempt -> {
                    Map<String, Object> map = new LinkedHashMap<>();

                    map.put("id", attempt.getId());
                    map.put("studentName", attempt.getStudent() == null ? "Student" : attempt.getStudent().getName());
                    map.put("email", attempt.getStudent() == null ? "" : attempt.getStudent().getEmail());
                    map.put("score", attempt.getScore() == null ? 0 : attempt.getScore());
                    map.put("totalMarks", attempt.getTotalMarks() == null ? 0 : attempt.getTotalMarks());
                    map.put("percentage", attempt.getPercentage() == null ? 0 : attempt.getPercentage());
                    map.put("status", attempt.getStatus());
                    map.put("submittedAt", attempt.getSubmittedAt());

                    return map;
                })
                .toList();
    }

    @Transactional
    public void deleteChallenge(Long id) {
        String email = securityUtils.getCurrentUserEmail();

        PseudoCodeChallenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        if (!email.equals(challenge.getTrainerEmail())) {
            throw new RuntimeException("Access denied");
        }

        challengeRepository.delete(challenge);
    }

    private Map<String, Object> mapChallengeListItem(PseudoCodeChallenge challenge) {
        Long attemptCount = attemptRepository.findByChallengeIdOrderBySubmittedAtDesc(challenge.getId())
                .stream()
                .count();

        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", challenge.getId());
        map.put("batchId", challenge.getBatchId());
        map.put("title", challenge.getTitle());
        map.put("problemStatement", challenge.getProblemStatement());
        map.put("durationMinutes", challenge.getDurationMinutes());
        map.put("totalMarks", challenge.getTotalMarks());
        map.put("passPercentage", challenge.getPassPercentage());
        map.put("active", Boolean.TRUE.equals(challenge.getActive()));
        map.put("createdAt", challenge.getCreatedAt());
        map.put("rulesCount", challenge.getRules() == null ? 0 : challenge.getRules().size());
        map.put("testCasesCount", challenge.getTestCases() == null ? 0 : challenge.getTestCases().size());
        map.put("attemptCount", attemptCount);
        map.put("challengeGroupId", challenge.getChallengeGroupId() == null ? "LEGACY-" + challenge.getId() : challenge.getChallengeGroupId());
        map.put("challengeGroupTitle", challenge.getChallengeGroupTitle() == null ? challenge.getTitle() : challenge.getChallengeGroupTitle());
        map.put("companyName", challenge.getCompanyName() == null ? "" : challenge.getCompanyName());

        return map;
    }

    private Map<String, Object> mapChallengeDetails(PseudoCodeChallenge challenge) {
        Map<String, Object> map = mapChallengeListItem(challenge);

        map.put("constraintsText", challenge.getConstraintsText());
        map.put("inputFormat", challenge.getInputFormat());
        map.put("outputFormat", challenge.getOutputFormat());

        map.put("rules", challenge.getRules().stream().map(rule -> {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", rule.getId());
            r.put("type", rule.getType());
            r.put("value", rule.getValue());
            r.put("marks", rule.getMarks());
            return r;
        }).toList());

        map.put("testCases", challenge.getTestCases().stream().map(tc -> {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("id", tc.getId());
            t.put("inputData", tc.getInputData());
            t.put("expectedOutput", tc.getExpectedOutput());
            t.put("marks", tc.getMarks());
            t.put("hidden", Boolean.TRUE.equals(tc.getHidden()));
            return t;
        }).toList());

        return map;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllSubmissions() {
        String email = securityUtils.getCurrentUserEmail();

        return attemptRepository.findByChallengeTrainerEmailOrderBySubmittedAtDesc(email)
                .stream()
                .map(this::mapSubmissionDetails)
                .toList();
    }

    private Map<String, Object> mapSubmissionDetails(PseudoCodeAttempt attempt) {
        Map<String, Object> map = new LinkedHashMap<>();

        PseudoCodeChallenge challenge = attempt.getChallenge();

        map.put("attemptId", attempt.getId());
        map.put("challengeId", challenge == null ? null : challenge.getId());
        map.put("challengeTitle", challenge == null ? "Challenge" : challenge.getTitle());
        map.put("problemStatement", challenge == null ? "" : challenge.getProblemStatement());
        map.put("batchId", challenge == null ? null : challenge.getBatchId());

        map.put(
                "challengeGroupId",
                challenge == null || challenge.getChallengeGroupId() == null
                        ? ""
                        : challenge.getChallengeGroupId()
        );
        map.put(
                "challengeGroupTitle",
                challenge == null || challenge.getChallengeGroupTitle() == null
                        ? ""
                        : challenge.getChallengeGroupTitle()
        );
        map.put(
                "companyName",
                challenge == null || challenge.getCompanyName() == null
                        ? ""
                        : challenge.getCompanyName()
        );

        map.put("studentId", attempt.getStudent() == null ? null : attempt.getStudent().getId());
        map.put("studentName", attempt.getStudent() == null ? "Student" : attempt.getStudent().getName());
        map.put("studentEmail", attempt.getStudent() == null ? "" : attempt.getStudent().getEmail());

        map.put("language", attempt.getLanguage());
        map.put("sourceCode", attempt.getSourceCode());
        map.put("score", attempt.getScore() == null ? 0 : attempt.getScore());
        map.put("totalMarks", attempt.getTotalMarks() == null ? 0 : attempt.getTotalMarks());
        map.put("percentage", attempt.getPercentage() == null ? 0 : attempt.getPercentage());
        map.put("status", attempt.getStatus());
        map.put("allTestsPassed", Boolean.TRUE.equals(attempt.getAllTestsPassed()));
        map.put("compileError", attempt.getCompileError());
        map.put("submittedAt", attempt.getSubmittedAt());

        map.put(
                "testResults",
                attempt.getOutputs().stream().map(output -> {
                    Map<String, Object> item = new LinkedHashMap<>();

                    item.put("testCaseId", output.getTestCaseId());
                    item.put("status", Boolean.TRUE.equals(output.getCorrect()) ? "PASS" : "FAIL");
                    item.put("inputData", output.getInputData());
                    item.put("expectedOutput", output.getExpectedOutput());
                    item.put("actualOutput", output.getActualOutput());
                    item.put("errorMessage", output.getErrorMessage());
                    item.put("marksObtained", output.getMarksObtained());
                    item.put("executionTimeMs", output.getExecutionTimeMs());

                    return item;
                }).toList()
        );

        return map;
    }

//    helper methods
private String getString(Map<?, ?> map, String key, String defaultValue) {
    Object value = map.get(key);
    return value == null ? defaultValue : String.valueOf(value);
}

    private Integer getInt(Map<?, ?> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        return Integer.valueOf(String.valueOf(value));
    }
}