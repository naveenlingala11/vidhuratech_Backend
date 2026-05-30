package com.vidhuratech.jobs.student.service;

import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.prep.entity.InterviewQuestion;
import com.vidhuratech.jobs.prep.repository.InterviewQuestionRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentInterviewQuestionService {

    private final InterviewQuestionRepository repo;
    private final BatchEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public Page<Map<String, Object>> myInterviewQuestions(
            String company,
            String role,
            String search,
            String type,
            String difficulty,
            String topic,
            int page
    ) {
        User student = getCurrentUser();

        List<Long> batchIds = enrollmentRepository.findActiveBatchIdsByStudentId(student.getId());

        if (batchIds == null || batchIds.isEmpty()) {
            return Page.empty(PageRequest.of(page, 10));
        }

        return repo.findStudentQuestions(
                batchIds,
                normalize(company),
                normalize(role),
                normalize(search),
                normalizeExact(type),
                normalizeExact(difficulty),
                normalize(topic),
                PageRequest.of(page, 10)
        ).map(this::toMap);
    }

    private User getCurrentUser() {
        Long userId = securityUtils.getCurrentUserId();

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Map<String, Object> toMap(InterviewQuestion q) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", q.getId());
        map.put("batchId", q.getBatchId());
        map.put("company", safe(q.getCompany()));
        map.put("role", safe(q.getRole()));
        map.put("type", safe(q.getType()));
        map.put("topic", safe(q.getTopic()));
        map.put("difficulty", safe(q.getDifficulty()));
        map.put("question", safe(q.getQuestion()));
        map.put("answer", safe(q.getAnswer()));
        map.put("active", Boolean.TRUE.equals(q.getActive()));
        map.put("publicVisible", Boolean.TRUE.equals(q.getPublicVisible()));
        map.put("publicAccessLevel", safe(q.getPublicAccessLevel()));
        map.put("createdAt", q.getCreatedAt());

        return map;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "" : value.trim().toLowerCase();
    }

    private String normalizeExact(String value) {
        return value == null || value.isBlank() ? "" : value.trim().toUpperCase();
    }
}