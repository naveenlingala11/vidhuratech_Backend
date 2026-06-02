package com.vidhuratech.jobs.trainer.service;

import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;
import com.vidhuratech.jobs.prep.entity.InterviewQuestion;
import com.vidhuratech.jobs.prep.repository.InterviewQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrainerInterviewQuestionService {

    private final InterviewQuestionRepository repo;
    private final BatchRepository batchRepository;
    private final SecurityUtils securityUtils;
    private final ActivityNotificationService notificationService;

    @Transactional
    public Map<String, Object> create(Map<String, Object> payload) {
        String email = securityUtils.getCurrentUserEmail();

        Long batchId = readLong(payload, "batchId", null);
        if (batchId == null) {
            throw new RuntimeException("Batch is required");
        }

        Batch batch = batchRepository.findByIdAndTrainerEmail(batchId, email)
                .orElseThrow(() -> new RuntimeException("Access denied for selected batch"));

        InterviewQuestion q = new InterviewQuestion();
        applyPayload(q, payload);

        q.setBatchId(batchId);
        q.setTrainer(batch.getTrainer());
        q.setActive(true);
        q.setPublicVisible(false);
        q.setPublicAccessLevel("LEAD_REQUIRED");
        q.setCreatedAt(LocalDateTime.now());

        InterviewQuestion saved = repo.save(q);

        notificationService.notifyAdmins(
                "New interview question posted",
                "Trainer posted interview question for " + safe(saved.getCompany()),
                "INTERVIEW_QUESTION_CREATED",
                "/dashboard/admin/public-practice"
        );

        if (batch.getEnrollments() != null) {
            notificationService.notifyBatchStudents(
                    batch.getEnrollments(),
                    "New interview question assigned",
                    "New " + safe(saved.getCompany()) + " interview question added",
                    "INTERVIEW_QUESTION_ASSIGNED",
                    "/dashboard/student/interview-questions"
            );
        }

        return map(saved);
    }

    @Transactional
    public Map<String, Object> bulkCreate(List<Map<String, Object>> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            throw new RuntimeException("No interview questions found");
        }

        String email = securityUtils.getCurrentUserEmail();

        List<Map<String, Object>> results = new ArrayList<>();
        List<InterviewQuestion> toSave = new ArrayList<>();
        Map<Long, Batch> batchesById = new LinkedHashMap<>();

        int failed = 0;

        for (Map<String, Object> item : payloads) {
            try {
                Long batchId = readLong(item, "batchId", null);
                if (batchId == null) {
                    throw new RuntimeException("Batch is required");
                }

                Batch batch = batchesById.computeIfAbsent(batchId, id ->
                        batchRepository.findByIdAndTrainerEmail(id, email)
                                .orElseThrow(() -> new RuntimeException("Access denied for selected batch"))
                );

                InterviewQuestion q = new InterviewQuestion();
                applyPayload(q, item);

                q.setBatchId(batchId);
                q.setTrainer(batch.getTrainer());
                q.setActive(true);
                q.setPublicVisible(false);
                q.setPublicAccessLevel("LEAD_REQUIRED");
                q.setCreatedAt(LocalDateTime.now());

                toSave.add(q);
            } catch (Exception e) {
                failed++;
                results.add(Map.of(
                        "question", String.valueOf(item.getOrDefault("question", "Unknown")),
                        "error", e.getMessage()
                ));
            }
        }

        List<InterviewQuestion> savedQuestions = repo.saveAll(toSave);
        savedQuestions.forEach(q -> results.add(map(q)));

        int success = savedQuestions.size();

        if (success > 0) {
            String summary = buildBulkSummary(savedQuestions, success);

            notificationService.notifyAdminsInAppOnly(
                    "Interview questions uploaded",
                    summary,
                    "INTERVIEW_QUESTIONS_BULK_CREATED",
                    "/dashboard/admin/public-practice"
            );

            for (Batch batch : batchesById.values()) {
                if (batch.getEnrollments() != null) {
                    notificationService.notifyBatchStudentsInAppOnly(
                            batch.getEnrollments(),
                            "New interview questions assigned",
                            summary,
                            "INTERVIEW_QUESTIONS_BULK_ASSIGNED",
                            "/dashboard/student/interview-questions"
                    );
                }
            }
        }

        return Map.of(
                "successCount", success,
                "failedCount", failed,
                "results", results
        );
    }

    public List<Map<String, Object>> list() {
        return repo.findByTrainerEmailOrderByCreatedAtDesc(securityUtils.getCurrentUserEmail())
                .stream()
                .map(this::map)
                .toList();
    }

    public Map<String, Object> details(Long id) {
        return map(requireTrainerQuestion(id));
    }

    @Transactional
    public Map<String, Object> update(Long id, Map<String, Object> payload) {
        InterviewQuestion q = requireTrainerQuestion(id);

        Long batchId = readLong(payload, "batchId", null);
        if (batchId == null) {
            throw new RuntimeException("Batch is required");
        }

        Batch batch = batchRepository.findByIdAndTrainerEmail(
                batchId,
                securityUtils.getCurrentUserEmail()
        ).orElseThrow(() -> new RuntimeException("Access denied for batch"));

        applyPayload(q, payload);
        q.setBatchId(batchId);
        q.setTrainer(batch.getTrainer());
        q.setUpdatedAt(LocalDateTime.now());

        return map(repo.save(q));
    }

    @Transactional
    public void delete(Long id) {
        InterviewQuestion q = requireTrainerQuestion(id);
        repo.delete(q);
    }

    private InterviewQuestion requireTrainerQuestion(Long id) {
        return repo.findByIdAndTrainerEmail(id, securityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new RuntimeException("Interview question not found or access denied"));
    }

    private void applyPayload(InterviewQuestion q, Map<String, Object> payload) {
        q.setCompany(read(payload, "company", "General"));
        q.setRole(read(payload, "role", "JAVA").toUpperCase());
        q.setType(read(payload, "type", "CONCEPTUAL").toUpperCase());
        q.setTopic(read(payload, "topic", "General"));
        q.setDifficulty(read(payload, "difficulty", "MEDIUM").toUpperCase());
        q.setQuestion(read(payload, "question", ""));
        q.setAnswer(read(payload, "answer", ""));

        if (q.getQuestion().isBlank()) {
            throw new RuntimeException("Question is required");
        }

        if (q.getAnswer().isBlank()) {
            throw new RuntimeException("Answer is required");
        }
    }

    private String read(Map<String, Object> payload, String key, String fallback) {
        Object value = payload.get(key);
        return value == null || String.valueOf(value).isBlank()
                ? fallback
                : String.valueOf(value).trim();
    }

    private Long readLong(Map<String, Object> payload, String key, Long fallback) {
        Object value = payload.get(key);

        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }

        return Long.valueOf(String.valueOf(value));
    }

    private String buildBulkSummary(List<InterviewQuestion> questions, int count) {
        String companies = questions.stream()
                .map(InterviewQuestion::getCompany)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .limit(3)
                .toList()
                .toString();

        String roles = questions.stream()
                .map(InterviewQuestion::getRole)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .limit(3)
                .toList()
                .toString();

        String topics = questions.stream()
                .map(InterviewQuestion::getTopic)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .limit(5)
                .toList()
                .toString();

        return count + " interview questions uploaded. Companies: "
                + companies + ", Roles: " + roles + ", Topics: " + topics;
    }

    private Map<String, Object> map(InterviewQuestion q) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", q.getId());
        map.put("batchId", q.getBatchId());
        map.put("company", safe(q.getCompany()));
        map.put("companyName", safe(q.getCompany()));
        map.put("role", safe(q.getRole()));
        map.put("skill", safe(q.getRole()));
        map.put("type", safe(q.getType()));
        map.put("topic", safe(q.getTopic()));
        map.put("difficulty", safe(q.getDifficulty()));
        map.put("question", safe(q.getQuestion()));
        map.put("title", safe(q.getQuestion()));
        map.put("answer", safe(q.getAnswer()));
        map.put("description", safe(q.getAnswer()));
        map.put("active", Boolean.TRUE.equals(q.getActive()));
        map.put("publicVisible", Boolean.TRUE.equals(q.getPublicVisible()));
        map.put("publicAccessLevel", safe(q.getPublicAccessLevel()));
        map.put("publishedAt", q.getPublishedAt());
        map.put("createdAt", q.getCreatedAt());

        return map;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}