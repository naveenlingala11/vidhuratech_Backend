package com.vidhuratech.jobs.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;
import com.vidhuratech.jobs.trainer.entity.Curriculum;
import com.vidhuratech.jobs.trainer.repository.CurriculumRepository;
import com.vidhuratech.jobs.trainer.service.TrainerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/lms/admin/course-manager")
@RequiredArgsConstructor
public class AdminCourseCurriculumController {

    private final CurriculumRepository curriculumRepository;
    private final BatchRepository batchRepository;
    private final ObjectMapper objectMapper;
    private final ActivityNotificationService notificationService;
    private final TrainerDashboardService trainerDashboardService;

    @GetMapping("/batches/{batchId}/curriculum")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ApiResponse<?> getCurriculum(@PathVariable Long batchId) {
        return ApiResponse.builder()
                .success(true)
                .data(curriculumRepository.findByBatchId(batchId).map(this::mapCurriculum).orElse(null))
                .build();
    }

    @PutMapping("/batches/{batchId}/curriculum")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ApiResponse<?> saveCurriculum(
            @PathVariable Long batchId,
            @RequestBody Map<String, Object> payload
    ) throws Exception {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        Object jsonObject = payload.get("json");
        String jsonData = jsonObject instanceof String
                ? String.valueOf(jsonObject)
                : objectMapper.writeValueAsString(jsonObject);

        objectMapper.readTree(jsonData);

        Curriculum curriculum = curriculumRepository.findByBatchId(batchId)
                .orElseGet(() -> Curriculum.builder()
                        .batchId(batchId)
                        .trainerEmail("admin")
                        .build());

        curriculum.setJsonData(jsonData);
        Curriculum saved = curriculumRepository.save(curriculum);

        if (Boolean.TRUE.equals(payload.get("notifyStudents"))) {
            notificationService.notifyBatchStudents(
                    batch.getEnrollments(),
                    "Curriculum updated",
                    "Curriculum updated for " + batch.getName(),
                    "CURRICULUM_UPDATED",
                    "/dashboard/student/lms/" + batchId
            );
        }

        return ApiResponse.builder()
                .success(true)
                .message("Curriculum saved successfully")
                .data(mapCurriculum(saved))
                .build();
    }

    @PostMapping("/batches/{batchId}/updates")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ApiResponse<?> shareBatchUpdate(
            @PathVariable Long batchId,
            @RequestBody Map<String, Object> payload
    ) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        String title = String.valueOf(payload.getOrDefault("title", "Batch update"));
        String message = String.valueOf(payload.getOrDefault("message", ""));
        String link = String.valueOf(payload.getOrDefault("link", "/dashboard/student/lms/" + batchId));

        notificationService.notifyBatchStudents(
                batch.getEnrollments(),
                title,
                message,
                "BATCH_UPDATE",
                link
        );

        return ApiResponse.builder()
                .success(true)
                .message("Batch update shared successfully")
                .build();
    }

    private Map<String, Object> mapCurriculum(Curriculum curriculum) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", curriculum.getId());
        map.put("batchId", curriculum.getBatchId());
        map.put("trainerEmail", curriculum.getTrainerEmail());
        map.put("jsonData", curriculum.getJsonData());
        return map;
    }

    @GetMapping("/curriculums/pending")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<?> getPendingCurriculums() {
        return ApiResponse.builder()
                .success(true)
                .data(trainerDashboardService.getPendingCurriculums())
                .build();
    }

    @PostMapping("/curriculums/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<?> publishCurriculum(@PathVariable Long id) {
        return ApiResponse.builder()
                .success(true)
                .message("Curriculum approved and published successfully")
                .data(trainerDashboardService.publishCurriculum(id))
                .build();
    }
}