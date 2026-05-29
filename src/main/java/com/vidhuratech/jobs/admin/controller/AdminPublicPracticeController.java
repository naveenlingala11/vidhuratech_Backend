package com.vidhuratech.jobs.admin.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.trainer.entity.Assessment;
import com.vidhuratech.jobs.trainer.entity.PseudoCodeChallenge;
import com.vidhuratech.jobs.trainer.repository.AssessmentRepository;
import com.vidhuratech.jobs.trainer.repository.PseudoCodeChallengeRepository;
import com.vidhuratech.jobs.leads.entity.Lead;
import com.vidhuratech.jobs.leads.repository.LeadRepository;
import com.vidhuratech.jobs.publicpractice.entity.PublicAssessmentAttempt;
import com.vidhuratech.jobs.publicpractice.entity.PublicChallengeAttempt;
import com.vidhuratech.jobs.publicpractice.repository.PublicAssessmentAttemptRepository;
import com.vidhuratech.jobs.publicpractice.repository.PublicChallengeAttemptRepository;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/public-practice")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
@RequiredArgsConstructor
public class AdminPublicPracticeController {

    private final PublicAssessmentAttemptRepository publicAssessmentAttemptRepository;
    private final PublicChallengeAttemptRepository publicChallengeAttemptRepository;
    private final LeadRepository leadRepository;
    private final AssessmentRepository assessmentRepository;
    private final PseudoCodeChallengeRepository challengeRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final ActivityNotificationService notificationService;
    @GetMapping("/candidates")
    public ApiResponse<?> candidates() {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("assessments", assessmentRepository.findAllPublicPracticeCandidates()
                .stream()
                .map(this::mapAssessment)
                .toList());

        data.put("challenges", challengeRepository.findAllPublicPracticeCandidates()
                .stream()
                .map(this::mapChallenge)
                .toList());

        return ApiResponse.success(data);
    }

    @PutMapping("/assessments/{id}/publish")
    public ApiResponse<?> publishAssessment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        Assessment assessment = assessmentRepository.findDetailedAssessment(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        applyAssessmentPublicSettings(assessment, payload);
        assessment.setPublicVisible(true);
        assessment.setPublishedAt(LocalDateTime.now());
        assessment.setPublishedByUserId(securityUtils.getCurrentUserId());

        assessmentRepository.save(assessment);
        if (assessment.getTrainer() != null) {
            notificationService.notifyTrainer(
                    assessment.getTrainer(),
                    "Assessment published",
                    "Your assessment is now public: " + assessment.getTitle(),
                    "ASSESSMENT_PUBLISHED",
                    "/dashboard/trainer/assessments"
            );
        }
        return ApiResponse.success(mapAssessment(assessment), "Assessment published successfully");
    }

    @PutMapping("/assessments/{id}/unpublish")
    public ApiResponse<?> unpublishAssessment(@PathVariable Long id) {
        Assessment assessment = assessmentRepository.findDetailedAssessment(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        assessment.setPublicVisible(false);
        assessmentRepository.save(assessment);
        if (assessment.getTrainer() != null) {
            notificationService.notifyTrainer(
                    assessment.getTrainer(),
                    "Assessment unpublished",
                    "Your assessment was removed from public practice: " + assessment.getTitle(),
                    "ASSESSMENT_UNPUBLISHED",
                    "/dashboard/trainer/assessments"
            );
        }
        return ApiResponse.success(mapAssessment(assessment), "Assessment unpublished successfully");
    }

    @PutMapping("/challenges/{id}/publish")
    public ApiResponse<?> publishChallenge(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        PseudoCodeChallenge challenge = challengeRepository.findPublicPracticeCandidateById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        applyChallengePublicSettings(challenge, payload);
        challenge.setPublicVisible(true);
        challenge.setPublishedAt(LocalDateTime.now());
        challenge.setPublishedByUserId(securityUtils.getCurrentUserId());

        challengeRepository.save(challenge);
        userRepository.findByEmail(challenge.getTrainerEmail()).ifPresent(trainer ->
                notificationService.notifyTrainer(
                        trainer,
                        "Coding challenge published",
                        "Your coding challenge is now public: " + challenge.getTitle(),
                        "CHALLENGE_PUBLISHED",
                        "/dashboard/trainer/pseudo-challenges"
                )
        );
        return ApiResponse.success(mapChallenge(challenge), "Challenge published successfully");
    }

    @PutMapping("/challenges/{id}/unpublish")
    public ApiResponse<?> unpublishChallenge(@PathVariable Long id) {
        PseudoCodeChallenge challenge = challengeRepository.findPublicPracticeCandidateById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        challenge.setPublicVisible(false);
        challengeRepository.save(challenge);
        userRepository.findByEmail(challenge.getTrainerEmail()).ifPresent(trainer ->
                notificationService.notifyTrainer(
                        trainer,
                        "Coding challenge unpublished",
                        "Your coding challenge was removed from public practice: " + challenge.getTitle(),
                        "CHALLENGE_UNPUBLISHED",
                        "/dashboard/trainer/pseudo-challenges"
                )
        );
        return ApiResponse.success(mapChallenge(challenge), "Challenge unpublished successfully");
    }

    @GetMapping("/attempts")
    public ApiResponse<?> attempts() {
        Map<String, Object> data = new LinkedHashMap<>();

        List<Map<String, Object>> assessmentAttempts =
                publicAssessmentAttemptRepository.findTop200ByOrderBySubmittedAtDesc()
                        .stream()
                        .map(this::mapPublicAssessmentAttempt)
                        .toList();

        List<Map<String, Object>> challengeAttempts =
                publicChallengeAttemptRepository.findTop200ByOrderBySubmittedAtDesc()
                        .stream()
                        .map(this::mapPublicChallengeAttempt)
                        .toList();

        data.put("assessmentAttempts", assessmentAttempts);
        data.put("challengeAttempts", challengeAttempts);
        data.put("totalAttempts", assessmentAttempts.size() + challengeAttempts.size());

        return ApiResponse.success(data);
    }

    @GetMapping("/assessments/{id}/attempts")
    public ApiResponse<?> assessmentAttempts(@PathVariable Long id) {
        return ApiResponse.success(
                publicAssessmentAttemptRepository.findByAssessmentIdOrderBySubmittedAtDesc(id)
                        .stream()
                        .map(this::mapPublicAssessmentAttempt)
                        .toList()
        );
    }

    @GetMapping("/challenges/{id}/attempts")
    public ApiResponse<?> challengeAttempts(@PathVariable Long id) {
        return ApiResponse.success(
                publicChallengeAttemptRepository.findByChallengeIdOrderBySubmittedAtDesc(id)
                        .stream()
                        .map(this::mapPublicChallengeAttempt)
                        .toList()
        );
    }

    @GetMapping("/access-policies")
    public ApiResponse<?> accessPolicies() {
        List<Map<String, Object>> policies = List.of(
                policy("PUBLIC_PREVIEW", "Visible publicly, attempt not allowed"),
                policy("LEAD_REQUIRED", "Guest must submit registration form before attempt"),
                policy("ACCOUNT_REQUIRED", "Login account required"),
                policy("ENROLLED_STUDENT_ONLY", "Only enrolled batch students can attempt"),
                policy("PAID_STUDENT_ONLY", "Only paid students can attempt")
        );

        return ApiResponse.success(policies);
    }

    private void applyAssessmentPublicSettings(Assessment assessment, Map<String, Object> payload) {
        assessment.setCompanyName(String.valueOf(payload.getOrDefault("companyName", "General")));
        assessment.setSkill(String.valueOf(payload.getOrDefault("skill", "Placement Readiness")));
        assessment.setPublicAccessLevel(String.valueOf(payload.getOrDefault("accessLevel", "LEAD_REQUIRED")));
        assessment.setPublicAttemptLimit(readInt(payload, "attemptLimit", 1));
    }

    private void applyChallengePublicSettings(PseudoCodeChallenge challenge, Map<String, Object> payload) {
        challenge.setCompanyName(String.valueOf(payload.getOrDefault("companyName", "General")));
        challenge.setSkill(String.valueOf(payload.getOrDefault("skill", "Coding")));
        challenge.setPublicAccessLevel(String.valueOf(payload.getOrDefault("accessLevel", "LEAD_REQUIRED")));
        challenge.setPublicAttemptLimit(readInt(payload, "attemptLimit", 1));
    }

    private Map<String, Object> mapAssessment(Assessment assessment) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", assessment.getId());
        map.put("type", "ASSESSMENT");
        map.put("title", assessment.getTitle());
        map.put("description", assessment.getDescription());

        map.put(
                "trainerEmail",
                assessment.getTrainer() == null ? "" : assessment.getTrainer().getEmail()
        );

        map.put(
                "trainerName",
                assessment.getTrainer() == null ? "" : assessment.getTrainer().getName()
        );

        map.put(
                "batchId",
                assessment.getBatch() == null ? null : assessment.getBatch().getId()
        );

        map.put(
                "batchName",
                assessment.getBatch() == null ? "" : assessment.getBatch().getName()
        );

        map.put(
                "courseName",
                assessment.getBatch() == null || assessment.getBatch().getCourse() == null
                        ? ""
                        : assessment.getBatch().getCourse().getTitle()
        );

        map.put(
                "companyName",
                assessment.getCompanyName() == null || assessment.getCompanyName().isBlank()
                        ? "General"
                        : assessment.getCompanyName()
        );

        map.put(
                "skill",
                assessment.getSkill() == null || assessment.getSkill().isBlank()
                        ? "Placement Readiness"
                        : assessment.getSkill()
        );

        map.put("active", Boolean.TRUE.equals(assessment.getActive()));
        map.put("publicVisible", Boolean.TRUE.equals(assessment.getPublicVisible()));
        map.put(
                "publicStatus",
                Boolean.TRUE.equals(assessment.getPublicVisible())
                        ? "PUBLISHED"
                        : "NOT_PUBLISHED"
        );
        map.put(
                "publicAccessLevel",
                assessment.getPublicAccessLevel() == null
                        ? "LEAD_REQUIRED"
                        : assessment.getPublicAccessLevel()
        );
        map.put(
                "publicAttemptLimit",
                assessment.getPublicAttemptLimit() == null
                        ? 1
                        : assessment.getPublicAttemptLimit()
        );
        map.put("publishedAt", assessment.getPublishedAt());
        map.put("publishedByUserId", assessment.getPublishedByUserId());
        map.put("questionCount", assessment.getQuestions() == null ? 0 : assessment.getQuestions().size());

        map.put(
                "publicAttemptCount",
                publicAssessmentAttemptRepository.countByAssessmentId(assessment.getId())
        );

        return map;
    }

    private Map<String, Object> mapChallenge(PseudoCodeChallenge challenge) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", challenge.getId());
        map.put("type", "CHALLENGE");
        map.put("title", challenge.getTitle());
        map.put("description", challenge.getProblemStatement());

        map.put("trainerEmail", challenge.getTrainerEmail());
        map.put("trainerName", challenge.getTrainerEmail());
        map.put("batchId", challenge.getBatchId());
        map.put("batchName", challenge.getBatchId() == null ? "" : "Batch #" + challenge.getBatchId());

        map.put(
                "companyName",
                challenge.getCompanyName() == null || challenge.getCompanyName().isBlank()
                        ? "General"
                        : challenge.getCompanyName()
        );

        map.put(
                "skill",
                challenge.getSkill() == null || challenge.getSkill().isBlank()
                        ? "Coding"
                        : challenge.getSkill()
        );

        map.put("challengeGroupId", challenge.getChallengeGroupId());
        map.put("challengeGroupTitle", challenge.getChallengeGroupTitle());

        map.put("active", Boolean.TRUE.equals(challenge.getActive()));
        map.put("publicVisible", Boolean.TRUE.equals(challenge.getPublicVisible()));
        map.put(
                "publicStatus",
                Boolean.TRUE.equals(challenge.getPublicVisible())
                        ? "PUBLISHED"
                        : "NOT_PUBLISHED"
        );
        map.put(
                "publicAccessLevel",
                challenge.getPublicAccessLevel() == null
                        ? "LEAD_REQUIRED"
                        : challenge.getPublicAccessLevel()
        );
        map.put(
                "publicAttemptLimit",
                challenge.getPublicAttemptLimit() == null
                        ? 1
                        : challenge.getPublicAttemptLimit()
        );
        map.put("publishedAt", challenge.getPublishedAt());
        map.put("publishedByUserId", challenge.getPublishedByUserId());
        map.put(
                "testCasesCount",
                challenge.getTestCases() == null
                        ? 0
                        : challenge.getTestCases().size()
        );
        map.put(
                "publicAttemptCount",
                publicChallengeAttemptRepository.countByChallengeId(challenge.getId())
        );

        return map;
    }

    private Map<String, Object> mapPublicAssessmentAttempt(PublicAssessmentAttempt attempt) {
        Map<String, Object> map = new LinkedHashMap<>();

        Lead lead = leadRepository.findById(attempt.getLeadId()).orElse(null);
        Assessment assessment = assessmentRepository.findById(attempt.getAssessmentId()).orElse(null);

        map.put("id", attempt.getId());
        map.put("type", "ASSESSMENT");
        map.put("accessGrantId", attempt.getAccessGrantId());
        map.put("leadId", attempt.getLeadId());
        map.put("leadName", lead == null ? "" : lead.getName());
        map.put("leadPhone", lead == null ? "" : lead.getPhone());
        map.put("leadEmail", lead == null ? "" : lead.getEmail());
        map.put("leadCity", lead == null ? "" : lead.getCity());

        map.put("practiceId", attempt.getAssessmentId());
        map.put("practiceTitle", assessment == null ? "Assessment" : assessment.getTitle());
        map.put("companyName", assessment == null ? "" : assessment.getCompanyName());
        map.put("skill", assessment == null ? "" : assessment.getSkill());

        map.put("score", attempt.getScore());
        map.put("totalMarks", attempt.getTotalMarks());
        map.put("percentage", attempt.getPercentage());
        map.put("correctAnswers", attempt.getCorrectAnswers());
        map.put("totalQuestions", attempt.getTotalQuestions());
        map.put("status", attempt.getStatus());
        map.put("submittedAt", attempt.getSubmittedAt());

        return map;
    }

    private Map<String, Object> mapPublicChallengeAttempt(PublicChallengeAttempt attempt) {
        Map<String, Object> map = new LinkedHashMap<>();

        Lead lead = leadRepository.findById(attempt.getLeadId()).orElse(null);
        PseudoCodeChallenge challenge = challengeRepository.findById(attempt.getChallengeId()).orElse(null);

        map.put("id", attempt.getId());
        map.put("type", "CHALLENGE");
        map.put("accessGrantId", attempt.getAccessGrantId());
        map.put("leadId", attempt.getLeadId());
        map.put("leadName", lead == null ? "" : lead.getName());
        map.put("leadPhone", lead == null ? "" : lead.getPhone());
        map.put("leadEmail", lead == null ? "" : lead.getEmail());
        map.put("leadCity", lead == null ? "" : lead.getCity());

        map.put("practiceId", attempt.getChallengeId());
        map.put("practiceTitle", challenge == null ? "Challenge" : challenge.getTitle());
        map.put("companyName", challenge == null ? "" : challenge.getCompanyName());
        map.put("skill", challenge == null ? "" : challenge.getSkill());

        map.put("language", attempt.getLanguage());
        map.put("sourceCode", attempt.getSourceCode());
        map.put("score", attempt.getScore());
        map.put("totalMarks", attempt.getTotalMarks());
        map.put("percentage", attempt.getPercentage());
        map.put("status", attempt.getStatus());
        map.put("submittedAt", attempt.getSubmittedAt());

        return map;
    }

    private Map<String, Object> policy(String value, String description) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("value", value);
        map.put("description", description);
        return map;
    }

    private int readInt(Map<String, Object> payload, String key, int defaultValue) {
        Object value = payload.get(key);
        return value == null ? defaultValue : Integer.parseInt(String.valueOf(value));
    }
}