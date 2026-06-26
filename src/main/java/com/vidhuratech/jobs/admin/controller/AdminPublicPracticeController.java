package com.vidhuratech.jobs.admin.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.leads.entity.Lead;
import com.vidhuratech.jobs.leads.repository.LeadRepository;
import com.vidhuratech.jobs.prep.entity.InterviewQuestion;
import com.vidhuratech.jobs.prep.repository.InterviewQuestionRepository;
import com.vidhuratech.jobs.publicpractice.entity.PublicAssessmentAttempt;
import com.vidhuratech.jobs.publicpractice.entity.PublicChallengeAttempt;
import com.vidhuratech.jobs.publicpractice.repository.PublicAssessmentAttemptRepository;
import com.vidhuratech.jobs.publicpractice.repository.PublicChallengeAttemptRepository;
import com.vidhuratech.jobs.trainer.entity.Assessment;
import com.vidhuratech.jobs.trainer.entity.PseudoCodeChallenge;
import com.vidhuratech.jobs.trainer.repository.AssessmentRepository;
import com.vidhuratech.jobs.trainer.repository.PseudoCodeChallengeRepository;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
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
    private final InterviewQuestionRepository interviewQuestionRepository;

    @Transactional(readOnly = true)
    @GetMapping("/candidates")
    public ApiResponse<?> candidates() {
        Map<String, Object> data = new LinkedHashMap<>();

        List<Assessment> assessments = assessmentRepository.findAllPublicPracticeCandidates();
        List<PseudoCodeChallenge> challenges = challengeRepository.findAllPublicPracticeCandidates();
        List<InterviewQuestion> interviewQuestions = interviewQuestionRepository.findAllPublicCandidates();

        List<Long> assessmentIds = assessments.stream().map(Assessment::getId).toList();
        List<Long> challengeIds = challenges.stream().map(PseudoCodeChallenge::getId).toList();

        Map<Long, Long> assessmentAttemptCounts = new HashMap<>();
        if (!assessmentIds.isEmpty()) {
            List<Object[]> counts = publicAssessmentAttemptRepository.countAttemptsForAssessments(assessmentIds);
            for (Object[] row : counts) {
                assessmentAttemptCounts.put((Long) row[0], (Long) row[1]);
            }
        }

        Map<Long, Long> challengeAttemptCounts = new HashMap<>();
        if (!challengeIds.isEmpty()) {
            List<Object[]> counts = publicChallengeAttemptRepository.countAttemptsForChallenges(challengeIds);
            for (Object[] row : counts) {
                challengeAttemptCounts.put((Long) row[0], (Long) row[1]);
            }
        }

        data.put("assessments", assessments
                .stream()
                .map(a -> mapAssessment(a, assessmentAttemptCounts.getOrDefault(a.getId(), 0L)))
                .toList());

        data.put("challenges", challenges
                .stream()
                .map(c -> mapChallenge(c, challengeAttemptCounts.getOrDefault(c.getId(), 0L)))
                .toList());

        data.put("interviewQuestions", interviewQuestions
                .stream()
                .map(this::mapInterviewQuestion)
                .toList());

        return ApiResponse.success(data);
    }

    @Transactional
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

        Assessment saved = assessmentRepository.save(assessment);

        if (saved.getTrainer() != null) {
            notificationService.notifyTrainer(
                    saved.getTrainer(),
                    "Assessment published",
                    "Your assessment is now public: " + saved.getTitle(),
                    "ASSESSMENT_PUBLISHED",
                    "/dashboard/trainer/assessments"
            );
        }

        return ApiResponse.success(mapAssessment(saved), "Assessment published successfully");
    }

    @Transactional
    @PutMapping("/assessments/{id}/unpublish")
    public ApiResponse<?> unpublishAssessment(@PathVariable Long id) {
        Assessment assessment = assessmentRepository.findDetailedAssessment(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        assessment.setPublicVisible(false);
        Assessment saved = assessmentRepository.save(assessment);

        if (saved.getTrainer() != null) {
            notificationService.notifyTrainer(
                    saved.getTrainer(),
                    "Assessment unpublished",
                    "Your assessment was removed from public practice: " + saved.getTitle(),
                    "ASSESSMENT_UNPUBLISHED",
                    "/dashboard/trainer/assessments"
            );
        }

        return ApiResponse.success(mapAssessment(saved), "Assessment unpublished successfully");
    }

    @Transactional
    @PutMapping("/challenges/{id}/publish")
    public ApiResponse<?> publishChallenge(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        PseudoCodeChallenge challenge = challengeRepository.findPublicPracticeCandidateById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found with id: " + id));

        applyChallengePublicSettings(challenge, payload);
        challenge.setPublicVisible(true);
        challenge.setPublishedAt(LocalDateTime.now());
        challenge.setPublishedByUserId(securityUtils.getCurrentUserId());

        PseudoCodeChallenge saved = challengeRepository.save(challenge);

        if (saved.getTrainerEmail() != null && !saved.getTrainerEmail().isBlank()) {
            userRepository.findByEmail(saved.getTrainerEmail()).ifPresent(trainer ->
                    notificationService.notifyTrainer(
                            trainer,
                            "Coding challenge published",
                            "Your coding challenge is now public: " + saved.getTitle(),
                            "CHALLENGE_PUBLISHED",
                            "/dashboard/trainer/pseudo-challenges"
                    )
            );
        }

        return ApiResponse.success(mapChallenge(saved), "Challenge published successfully");
    }

    @Transactional
    @PutMapping("/challenges/{id}/unpublish")
    public ApiResponse<?> unpublishChallenge(@PathVariable Long id) {
        PseudoCodeChallenge challenge = challengeRepository.findPublicPracticeCandidateById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found with id: " + id));

        challenge.setPublicVisible(false);
        PseudoCodeChallenge saved = challengeRepository.save(challenge);

        if (saved.getTrainerEmail() != null && !saved.getTrainerEmail().isBlank()) {
            userRepository.findByEmail(saved.getTrainerEmail()).ifPresent(trainer ->
                    notificationService.notifyTrainer(
                            trainer,
                            "Coding challenge unpublished",
                            "Your coding challenge was removed from public practice: " + saved.getTitle(),
                            "CHALLENGE_UNPUBLISHED",
                            "/dashboard/trainer/pseudo-challenges"
                    )
            );
        }

        return ApiResponse.success(mapChallenge(saved), "Challenge unpublished successfully");
    }

    @Transactional
    @PutMapping("/interview-questions/{id}/publish")
    public ApiResponse<?> publishInterviewQuestion(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        InterviewQuestion question = interviewQuestionRepository.findPublicPracticeCandidateById(id)
                .orElseThrow(() -> new RuntimeException("Interview question not found with id: " + id));

        applyInterviewQuestionPublicSettings(question, payload);
        question.setPublicVisible(true);
        question.setPublishedAt(LocalDateTime.now());
        question.setPublishedByUserId(securityUtils.getCurrentUserId());

        InterviewQuestion saved = interviewQuestionRepository.save(question);

        if (saved.getTrainer() != null) {
            notificationService.notifyTrainer(
                    saved.getTrainer(),
                    "Interview question published",
                    "Your interview question is now public: " + safe(saved.getQuestion()),
                    "INTERVIEW_QUESTION_PUBLISHED",
                    "/dashboard/trainer/interview-questions"
            );
        }

        return ApiResponse.success(mapInterviewQuestion(saved), "Interview question published successfully");
    }

    @Transactional
    @PutMapping("/interview-questions/{id}/unpublish")
    public ApiResponse<?> unpublishInterviewQuestion(@PathVariable Long id) {
        InterviewQuestion question = interviewQuestionRepository.findPublicPracticeCandidateById(id)
                .orElseThrow(() -> new RuntimeException("Interview question not found with id: " + id));

        question.setPublicVisible(false);
        InterviewQuestion saved = interviewQuestionRepository.save(question);

        if (saved.getTrainer() != null) {
            notificationService.notifyTrainer(
                    saved.getTrainer(),
                    "Interview question unpublished",
                    "Your interview question was removed from public practice",
                    "INTERVIEW_QUESTION_UNPUBLISHED",
                    "/dashboard/trainer/interview-questions"
            );
        }

        return ApiResponse.success(mapInterviewQuestion(saved), "Interview question unpublished successfully");
    }

    @Transactional
    @PutMapping("/bulk/publish")
    public ApiResponse<?> bulkPublish(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = bulkUpdatePublicPractice(payload, true);
        return ApiResponse.success(result, "Bulk publish completed successfully");
    }

    @Transactional
    @PutMapping("/bulk/unpublish")
    public ApiResponse<?> bulkUnpublish(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = bulkUpdatePublicPractice(payload, false);
        return ApiResponse.success(result, "Bulk unpublish completed successfully");
    }

    @Transactional(readOnly = true)
    @GetMapping("/attempts")
    public ApiResponse<?> attempts() {
        Map<String, Object> data = new LinkedHashMap<>();

        List<PublicAssessmentAttempt> assessmentAttempts =
                publicAssessmentAttemptRepository.findTop200ByOrderBySubmittedAtDesc();

        List<PublicChallengeAttempt> challengeAttempts =
                publicChallengeAttemptRepository.findTop200ByOrderBySubmittedAtDesc();

        Set<Long> leadIds = new HashSet<>();
        Set<Long> assessmentIds = new HashSet<>();
        Set<Long> challengeIds = new HashSet<>();

        for (PublicAssessmentAttempt attempt : assessmentAttempts) {
            if (attempt.getLeadId() != null) leadIds.add(attempt.getLeadId());
            if (attempt.getAssessmentId() != null) assessmentIds.add(attempt.getAssessmentId());
        }

        for (PublicChallengeAttempt attempt : challengeAttempts) {
            if (attempt.getLeadId() != null) leadIds.add(attempt.getLeadId());
            if (attempt.getChallengeId() != null) challengeIds.add(attempt.getChallengeId());
        }

        Map<Long, Lead> leadsMap = new HashMap<>();
        if (!leadIds.isEmpty()) {
            leadRepository.findAllById(leadIds).forEach(l -> leadsMap.put(l.getId(), l));
        }

        Map<Long, Assessment> assessmentsMap = new HashMap<>();
        if (!assessmentIds.isEmpty()) {
            assessmentRepository.findAllById(assessmentIds).forEach(a -> assessmentsMap.put(a.getId(), a));
        }

        Map<Long, PseudoCodeChallenge> challengesMap = new HashMap<>();
        if (!challengeIds.isEmpty()) {
            challengeRepository.findAllById(challengeIds).forEach(c -> challengesMap.put(c.getId(), c));
        }

        List<Map<String, Object>> assessmentAttemptsMapped = assessmentAttempts.stream()
                .map(attempt -> mapPublicAssessmentAttempt(attempt, leadsMap.get(attempt.getLeadId()), assessmentsMap.get(attempt.getAssessmentId())))
                .toList();

        List<Map<String, Object>> challengeAttemptsMapped = challengeAttempts.stream()
                .map(attempt -> mapPublicChallengeAttempt(attempt, leadsMap.get(attempt.getLeadId()), challengesMap.get(attempt.getChallengeId())))
                .toList();

        data.put("assessmentAttempts", assessmentAttemptsMapped);
        data.put("challengeAttempts", challengeAttemptsMapped);
        data.put("totalAttempts", assessmentAttemptsMapped.size() + challengeAttemptsMapped.size());

        return ApiResponse.success(data);
    }

    @Transactional(readOnly = true)
    @GetMapping("/assessments/{id}/attempts")
    public ApiResponse<?> assessmentAttempts(@PathVariable Long id) {
        List<PublicAssessmentAttempt> attempts = publicAssessmentAttemptRepository.findByAssessmentIdOrderBySubmittedAtDesc(id);

        Set<Long> leadIds = new HashSet<>();
        for (PublicAssessmentAttempt attempt : attempts) {
            if (attempt.getLeadId() != null) leadIds.add(attempt.getLeadId());
        }

        Map<Long, Lead> leadsMap = new HashMap<>();
        if (!leadIds.isEmpty()) {
            leadRepository.findAllById(leadIds).forEach(l -> leadsMap.put(l.getId(), l));
        }

        Assessment assessment = assessmentRepository.findById(id).orElse(null);

        return ApiResponse.success(
                attempts.stream()
                        .map(attempt -> mapPublicAssessmentAttempt(attempt, leadsMap.get(attempt.getLeadId()), assessment))
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    @GetMapping("/challenges/{id}/attempts")
    public ApiResponse<?> challengeAttempts(@PathVariable Long id) {
        List<PublicChallengeAttempt> attempts = publicChallengeAttemptRepository.findByChallengeIdOrderBySubmittedAtDesc(id);

        Set<Long> leadIds = new HashSet<>();
        for (PublicChallengeAttempt attempt : attempts) {
            if (attempt.getLeadId() != null) leadIds.add(attempt.getLeadId());
        }

        Map<Long, Lead> leadsMap = new HashMap<>();
        if (!leadIds.isEmpty()) {
            leadRepository.findAllById(leadIds).forEach(l -> leadsMap.put(l.getId(), l));
        }

        PseudoCodeChallenge challenge = challengeRepository.findById(id).orElse(null);

        return ApiResponse.success(
                attempts.stream()
                        .map(attempt -> mapPublicChallengeAttempt(attempt, leadsMap.get(attempt.getLeadId()), challenge))
                        .toList()
        );
    }

    @GetMapping("/access-policies")
    public ApiResponse<?> accessPolicies() {
        List<Map<String, Object>> policies = List.of(
                policy("PUBLIC_PREVIEW", "Visible publicly, attempt not allowed"),
                policy("LEAD_REQUIRED", "Guest must submit registration form before attempt"),
                policy("ACCOUNT_REQUIRED", "Login account required"),
                policy("BASIC_PLAN", "Basic, Pro, and Elite plan users can attempt"),
                policy("PRO_PLAN", "Pro and Elite plan users can attempt"),
                policy("ELITE_PLAN", "Only Elite plan users can attempt"),
                policy("ENROLLED_STUDENT_ONLY", "Only enrolled batch students can attempt"),
                policy("PAID_STUDENT_ONLY", "Only active paid users can attempt")
        );

        return ApiResponse.success(policies);
    }

    private Map<String, Object> bulkUpdatePublicPractice(
            Map<String, Object> payload,
            boolean publish
    ) {
        String type = readText(payload, "type", "").toUpperCase(Locale.ROOT);
        List<Long> ids = readIds(payload.get("ids"));

        if (ids.isEmpty()) {
            throw new RuntimeException("Please select at least one item");
        }

        LocalDateTime now = LocalDateTime.now();
        Long currentUserId = securityUtils.getCurrentUserId();
        int requested = ids.size();
        int updated = 0;
        int skippedInactive = 0;
        List<Map<String, Object>> items = new ArrayList<>();

        if ("ASSESSMENT".equals(type)) {
            for (Long id : ids) {
                Assessment assessment = assessmentRepository.findDetailedAssessment(id).orElse(null);
                if (assessment == null) continue;

                if (publish && !Boolean.TRUE.equals(assessment.getActive())) {
                    skippedInactive++;
                    continue;
                }

                if (publish) {
                    applyAssessmentBulkSettings(assessment, payload);
                    assessment.setPublicVisible(true);
                    assessment.setPublishedAt(now);
                    assessment.setPublishedByUserId(currentUserId);
                } else {
                    assessment.setPublicVisible(false);
                }

                items.add(mapAssessment(assessmentRepository.save(assessment)));
                updated++;
            }
        } else if ("CHALLENGE".equals(type)) {
            for (Long id : ids) {
                PseudoCodeChallenge challenge = challengeRepository.findPublicPracticeCandidateById(id).orElse(null);
                if (challenge == null) continue;

                if (publish && !Boolean.TRUE.equals(challenge.getActive())) {
                    skippedInactive++;
                    continue;
                }

                if (publish) {
                    applyChallengeBulkSettings(challenge, payload);
                    challenge.setPublicVisible(true);
                    challenge.setPublishedAt(now);
                    challenge.setPublishedByUserId(currentUserId);
                } else {
                    challenge.setPublicVisible(false);
                }

                items.add(mapChallenge(challengeRepository.save(challenge)));
                updated++;
            }
        } else if ("INTERVIEW".equals(type)) {
            for (Long id : ids) {
                InterviewQuestion question = interviewQuestionRepository.findPublicPracticeCandidateById(id).orElse(null);
                if (question == null) continue;

                if (publish && !Boolean.TRUE.equals(question.getActive())) {
                    skippedInactive++;
                    continue;
                }

                if (publish) {
                    applyInterviewQuestionPublicSettings(question, payload);
                    question.setPublicVisible(true);
                    question.setPublishedAt(now);
                    question.setPublishedByUserId(currentUserId);
                } else {
                    question.setPublicVisible(false);
                }

                items.add(mapInterviewQuestion(interviewQuestionRepository.save(question)));
                updated++;
            }
        } else {
            throw new RuntimeException("Unsupported practice type");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", type);
        result.put("requested", requested);
        result.put("updated", updated);
        result.put("skippedInactive", skippedInactive);
        result.put("items", items);
        return result;
    }

    private void applyInterviewQuestionPublicSettings(
            InterviewQuestion question,
            Map<String, Object> payload
    ) {
        question.setCompany(readText(payload, "companyName", safe(question.getCompany())));
        question.setRole(readText(payload, "skill", safe(question.getRole())));
        question.setPublicAccessLevel(resolvePublicAccessLevel(payload, question.getPublicAccessLevel()));
    }

    private void applyAssessmentPublicSettings(Assessment assessment, Map<String, Object> payload) {
        assessment.setCompanyName(readText(payload, "companyName", "General"));
        assessment.setSkill(readText(payload, "skill", "Placement Readiness"));
        assessment.setPublicAccessLevel(resolvePublicAccessLevel(payload, assessment.getPublicAccessLevel()));
        assessment.setPublicAttemptLimit(readInt(payload, "attemptLimit", 1));
    }

    private void applyChallengePublicSettings(PseudoCodeChallenge challenge, Map<String, Object> payload) {
        challenge.setCompanyName(readText(payload, "companyName", "General"));
        challenge.setSkill(readText(payload, "skill", "Coding"));
        challenge.setPublicAccessLevel(resolvePublicAccessLevel(payload, challenge.getPublicAccessLevel()));
        challenge.setPublicAttemptLimit(readInt(payload, "attemptLimit", 1));
    }

    private void applyAssessmentBulkSettings(Assessment assessment, Map<String, Object> payload) {
        assessment.setCompanyName(readText(
                payload,
                "companyName",
                assessment.getCompanyName() == null || assessment.getCompanyName().isBlank()
                        ? "General"
                        : assessment.getCompanyName()
        ));

        assessment.setSkill(readText(
                payload,
                "skill",
                assessment.getSkill() == null || assessment.getSkill().isBlank()
                        ? "Placement Readiness"
                        : assessment.getSkill()
        ));

        assessment.setPublicAccessLevel(resolvePublicAccessLevel(payload, assessment.getPublicAccessLevel()));
        assessment.setPublicAttemptLimit(readInt(payload, "attemptLimit", 1));
    }

    private void applyChallengeBulkSettings(PseudoCodeChallenge challenge, Map<String, Object> payload) {
        challenge.setCompanyName(readText(
                payload,
                "companyName",
                challenge.getCompanyName() == null || challenge.getCompanyName().isBlank()
                        ? "General"
                        : challenge.getCompanyName()
        ));

        challenge.setSkill(readText(
                payload,
                "skill",
                challenge.getSkill() == null || challenge.getSkill().isBlank()
                        ? "Coding"
                        : challenge.getSkill()
        ));

        challenge.setPublicAccessLevel(resolvePublicAccessLevel(payload, challenge.getPublicAccessLevel()));
        challenge.setPublicAttemptLimit(readInt(payload, "attemptLimit", 1));
    }

    private Map<String, Object> mapInterviewQuestion(InterviewQuestion q) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", q.getId());
        map.put("type", "INTERVIEW");
        map.put("title", safe(q.getQuestion()));
        map.put("description", safe(q.getAnswer()));
        map.put("question", safe(q.getQuestion()));
        map.put("answer", safe(q.getAnswer()));
        map.put("company", safe(q.getCompany()));
        map.put("companyName", safe(q.getCompany()));
        map.put("role", safe(q.getRole()));
        map.put("skill", safe(q.getRole()));
        map.put("topic", safe(q.getTopic()));
        map.put("difficulty", safe(q.getDifficulty()));
        map.put("batchId", q.getBatchId());
        map.put("batchName", q.getBatchId() == null ? "" : "Batch #" + q.getBatchId());
        map.put("active", Boolean.TRUE.equals(q.getActive()));
        map.put("publicVisible", Boolean.TRUE.equals(q.getPublicVisible()));
        map.put("publicAccessLevel", q.getPublicAccessLevel() == null ? "LEAD_REQUIRED" : q.getPublicAccessLevel());
        map.put("publicAttemptLimit", 1);
        map.put("publicAttemptCount", 0);
        map.put("publishedAt", q.getPublishedAt());
        map.put("publishedByUserId", q.getPublishedByUserId());
        map.put("createdAt", q.getCreatedAt());

        if (q.getTrainer() != null) {
            map.put("trainerId", q.getTrainer().getId());
            map.put("trainerName", safe(q.getTrainer().getName()));
            map.put("trainerEmail", safe(q.getTrainer().getEmail()));
        } else {
            map.put("trainerId", null);
            map.put("trainerName", "");
            map.put("trainerEmail", "");
        }

        return map;
    }

    private Map<String, Object> mapAssessment(Assessment assessment) {
        long count = publicAssessmentAttemptRepository.countByAssessmentId(assessment.getId());
        return mapAssessment(assessment, count);
    }

    private Map<String, Object> mapAssessment(Assessment assessment, long publicAttemptCount) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", assessment.getId());
        map.put("type", "ASSESSMENT");
        map.put("title", assessment.getTitle());
        map.put("description", assessment.getDescription());
        map.put("trainerEmail", assessment.getTrainer() == null ? "" : assessment.getTrainer().getEmail());
        map.put("trainerName", assessment.getTrainer() == null ? "" : assessment.getTrainer().getName());
        map.put("batchId", assessment.getBatch() == null ? null : assessment.getBatch().getId());
        map.put("batchName", assessment.getBatch() == null ? "" : assessment.getBatch().getName());
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
        map.put("publicStatus", Boolean.TRUE.equals(assessment.getPublicVisible()) ? "PUBLISHED" : "NOT_PUBLISHED");
        map.put("publicAccessLevel", assessment.getPublicAccessLevel() == null ? "LEAD_REQUIRED" : assessment.getPublicAccessLevel());
        map.put("publicAttemptLimit", assessment.getPublicAttemptLimit() == null ? 1 : assessment.getPublicAttemptLimit());
        map.put("publishedAt", assessment.getPublishedAt());
        map.put("publishedByUserId", assessment.getPublishedByUserId());
        map.put("questionCount", assessment.getQuestions() == null ? 0 : assessment.getQuestions().size());
        map.put("publicAttemptCount", publicAttemptCount);

        return map;
    }

    private Map<String, Object> mapChallenge(PseudoCodeChallenge challenge) {
        long count = publicChallengeAttemptRepository.countByChallengeId(challenge.getId());
        return mapChallenge(challenge, count);
    }

    private Map<String, Object> mapChallenge(PseudoCodeChallenge challenge, long publicAttemptCount) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", challenge.getId());
        map.put("type", "CHALLENGE");
        map.put("title", challenge.getTitle());
        map.put("description", challenge.getProblemStatement());
        map.put("trainerEmail", challenge.getTrainerEmail());
        map.put("trainerName", challenge.getTrainerEmail());
        map.put("batchId", challenge.getBatchId());
        map.put("batchName", challenge.getChallengeGroupId() == null ? "" : "Batch #" + challenge.getBatchId());
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
        map.put("publicStatus", Boolean.TRUE.equals(challenge.getPublicVisible()) ? "PUBLISHED" : "NOT_PUBLISHED");
        map.put("publicAccessLevel", challenge.getPublicAccessLevel() == null ? "LEAD_REQUIRED" : challenge.getPublicAccessLevel());
        map.put("publicAttemptLimit", challenge.getPublicAttemptLimit() == null ? 1 : challenge.getPublicAttemptLimit());
        map.put("publishedAt", challenge.getPublishedAt());
        map.put("publishedByUserId", challenge.getPublishedByUserId());
        map.put("testCasesCount", challenge.getTestCases() == null ? 0 : challenge.getTestCases().size());
        map.put("publicAttemptCount", publicAttemptCount);

        return map;
    }

    private Map<String, Object> mapPublicAssessmentAttempt(PublicAssessmentAttempt attempt) {
        Lead lead = leadRepository.findById(attempt.getLeadId()).orElse(null);
        Assessment assessment = assessmentRepository.findById(attempt.getAssessmentId()).orElse(null);
        return mapPublicAssessmentAttempt(attempt, lead, assessment);
    }

    private Map<String, Object> mapPublicAssessmentAttempt(PublicAssessmentAttempt attempt, Lead lead, Assessment assessment) {
        Map<String, Object> map = new LinkedHashMap<>();

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
        Lead lead = leadRepository.findById(attempt.getLeadId()).orElse(null);
        PseudoCodeChallenge challenge = challengeRepository.findById(attempt.getChallengeId()).orElse(null);
        return mapPublicChallengeAttempt(attempt, lead, challenge);
    }

    private Map<String, Object> mapPublicChallengeAttempt(PublicChallengeAttempt attempt, Lead lead, PseudoCodeChallenge challenge) {
        Map<String, Object> map = new LinkedHashMap<>();

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

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String resolvePublicAccessLevel(Map<String, Object> payload, String defaultValue) {
        List<String> selectedLevels = readAccessLevels(payload);

        if (selectedLevels.isEmpty()) {
            selectedLevels = List.of(readText(payload, "accessLevel", defaultValue == null ? "LEAD_REQUIRED" : defaultValue));
        }

        return selectedLevels.stream()
                .map(this::normalizeAccessLevel)
                .distinct()
                .max(Comparator.comparingInt(this::accessPriority))
                .orElse("LEAD_REQUIRED");
    }

    private List<String> readAccessLevels(Map<String, Object> payload) {
        if (payload == null) return List.of();

        Object raw = payload.get("accessLevels");

        if (raw instanceof Collection<?> values) {
            return values.stream()
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .toList();
        }

        if (raw != null && !String.valueOf(raw).isBlank()) {
            return Arrays.stream(String.valueOf(raw).split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .toList();
        }

        return List.of();
    }

    private String normalizeAccessLevel(String value) {
        String code = String.valueOf(value == null ? "" : value)
                .trim()
                .toUpperCase(Locale.ROOT);

        if (code.contains("ELITE")) return "ELITE_PLAN";
        if (code.contains("PRO")) return "PRO_PLAN";
        if (code.contains("BASIC") || code.contains("STARTER")) return "BASIC_PLAN";
        if (code.equals("PAID") || code.equals("PAID_ONLY") || code.equals("PAID_STUDENT")) {
            return "PAID_STUDENT_ONLY";
        }
        if (code.equals("PUBLIC") || code.equals("PUBLIC_PREVIEW")) return "PUBLIC_PREVIEW";
        if (code.equals("ACCOUNT") || code.equals("ACCOUNT_REQUIRED")) return "ACCOUNT_REQUIRED";
        if (code.equals("ENROLLED") || code.equals("ENROLLED_STUDENT")) {
            return "ENROLLED_STUDENT_ONLY";
        }
        if (code.equals("LEAD") || code.equals("LEAD_REQUIRED")) return "LEAD_REQUIRED";

        return "LEAD_REQUIRED";
    }

    private int accessPriority(String accessLevel) {
        return switch (normalizeAccessLevel(accessLevel)) {
            case "PUBLIC_PREVIEW" -> 1;
            case "LEAD_REQUIRED" -> 2;
            case "ACCOUNT_REQUIRED" -> 3;
            case "ENROLLED_STUDENT_ONLY" -> 4;
            case "PAID_STUDENT_ONLY" -> 5;
            case "BASIC_PLAN" -> 6;
            case "PRO_PLAN" -> 7;
            case "ELITE_PLAN" -> 8;
            default -> 2;
        };
    }

    private String readText(Map<String, Object> payload, String key, String defaultValue) {
        if (payload == null) return defaultValue;

        Object value = payload.get(key);

        if (value == null || String.valueOf(value).isBlank()) {
            return defaultValue;
        }

        return String.valueOf(value).trim();
    }

    private int readInt(Map<String, Object> payload, String key, int defaultValue) {
        if (payload == null) return defaultValue;

        Object value = payload.get(key);

        if (value == null || String.valueOf(value).isBlank()) {
            return defaultValue;
        }

        try {
            int parsed = Integer.parseInt(String.valueOf(value).trim());
            return parsed < 1 ? defaultValue : parsed;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private List<Long> readIds(Object rawIds) {
        if (!(rawIds instanceof Collection<?> values)) {
            return List.of();
        }

        return values.stream()
                .map(value -> {
                    try {
                        return Long.parseLong(String.valueOf(value));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}