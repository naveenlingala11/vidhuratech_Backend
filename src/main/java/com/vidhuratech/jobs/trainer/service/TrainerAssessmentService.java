package com.vidhuratech.jobs.trainer.service;

import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;
import com.vidhuratech.jobs.trainer.entity.Assessment;
import com.vidhuratech.jobs.trainer.entity.AssessmentAttempt;
import com.vidhuratech.jobs.trainer.entity.AssessmentQuestion;
import com.vidhuratech.jobs.trainer.repository.AssessmentAttemptRepository;
import com.vidhuratech.jobs.trainer.repository.AssessmentQuestionRepository;
import com.vidhuratech.jobs.trainer.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrainerAssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository questionRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final BatchRepository batchRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public Map<String, Object> createAssessment(
            Map<String, Object> payload
    ) {

        Long batchId = Long.valueOf(
                String.valueOf(payload.get("batchId"))
        );

        Batch batch = batchRepository
                .findById(batchId)
                .orElseThrow(() ->
                        new RuntimeException("Batch not found"));

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
                .trainer(batch.getTrainer())
                .active(true)
                .build();

        assessment = assessmentRepository.save(assessment);

        Object rawQuestions = payload.get("questions");

        if (!(rawQuestions instanceof List<?> rawList)) {
            throw new RuntimeException("Invalid questions payload");
        }

        for (Object obj : rawList) {

            if (!(obj instanceof Map<?, ?> q)) {
                throw new RuntimeException("Invalid question format");
            }

            AssessmentQuestion question =
                    AssessmentQuestion.builder()
                            .assessment(assessment)
                            .question(String.valueOf(q.get("question")))
                            .optionA(String.valueOf(q.get("optionA")))
                            .optionB(String.valueOf(q.get("optionB")))
                            .optionC(String.valueOf(q.get("optionC")))
                            .optionD(String.valueOf(q.get("optionD")))
                            .correctAnswer(
                                    String.valueOf(q.get("correctAnswer"))
                                            .toUpperCase()
                            )
                            .marks(Integer.valueOf(
                                    String.valueOf(q.get("marks"))
                            ))
                            .build();

            assessment.getQuestions().add(question);
        }

        assessmentRepository.save(assessment);

        Map<String, Object> response = new HashMap<>();

        response.put("assessmentId", assessment.getId());
        response.put("title", assessment.getTitle());
        response.put("message", "Assessment created successfully");

        return response;
    }

    public List<Assessment> getTrainerAssessments() {

        String email = securityUtils.getCurrentUserEmail();

        return assessmentRepository.findByTrainerEmail(email);
    }

    public List<AssessmentAttempt> getAssessmentAttempts(
            Long assessmentId
    ) {

        return attemptRepository.findByAssessmentId(
                assessmentId
        );
    }
}