package com.vidhuratech.jobs.trainer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.trainer.entity.Curriculum;
import com.vidhuratech.jobs.trainer.entity.TrainingContent;
import com.vidhuratech.jobs.trainer.entity.TrainingContentType;
import com.vidhuratech.jobs.trainer.service.TrainerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
public class TrainerDashboardController {

    private final TrainerDashboardService service;

    // ✅ DASHBOARD
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> getDashboard() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getDashboard())
                .build();
    }

    // ✅ BATCHES
    @GetMapping("/batches")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> getBatches() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getBatches())
                .build();
    }

    // ✅ STUDENTS
    @GetMapping("/students")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> getStudents() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getStudents())
                .build();
    }

    // ✅ UPLOAD CURRICULUM
    @PostMapping("/upload-curriculum")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<?> uploadCurriculum(
            @RequestParam MultipartFile file,
            @RequestParam Long batchId
    ) throws Exception {

        String content = new String(file.getBytes());

        service.saveOrUpdateCurriculum(batchId, content);

        return ResponseEntity.ok("Uploaded");
    }

    // ✅ GET CURRICULUM
    @GetMapping("/curriculum")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> getCurriculum(@RequestParam Long batchId) {

        return ApiResponse.builder()
                .success(true)
                .data(
                        service.getCurriculum(batchId)
                                .map(Curriculum::getJsonData)
                                .orElse(null)
                )
                .build();
    }

    @PostMapping("/upload-json-curriculum")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<?> uploadJson(
            @RequestBody Map<String, Object> payload
    ) throws JsonProcessingException {

        Long batchId = Long.valueOf(payload.get("batchId").toString());
        Object jsonObj = payload.get("json");

        String json;

        if (jsonObj instanceof String) {
            json = (String) jsonObj;
        } else {
            json = new ObjectMapper().writeValueAsString(jsonObj);
        }
        service.saveOrUpdateCurriculum(batchId, json);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Curriculum saved successfully")
                        .build()
        );
    }

    // PUBLIC ENDPOINT - no @PreAuthorize needed
    @GetMapping("/public-curriculum")
    public ApiResponse<?> publicCurriculum(@RequestParam Long batchId) {

        String data = service.getCurriculumPreview(batchId);

        if (data == null) {
            return ApiResponse.builder()
                    .success(false)
                    .message("Curriculum not found")
                    .data(null)
                    .build();
        }

        return ApiResponse.builder()
                .success(true)
                .data(data)
                .build();
    }

    @PostMapping("/content")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> uploadContent(
            @RequestParam Long batchId,
            @RequestParam TrainingContentType type,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String jsonData,
            @RequestParam(required = false) String links,
            @RequestParam(required = false) MultipartFile file
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Content uploaded successfully")
                .data(service.uploadContent(batchId, type, title, description, file, jsonData, links))
                .build();
    }

    @GetMapping("/content")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> getContent() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getTrainerContent())
                .build();
    }

    @GetMapping("/content/{id}/file")
    public ResponseEntity<?> downloadContentFile(@PathVariable Long id) {
        TrainingContent content = service.getContentFile(id);

        if (content.getFileUrl() != null && !content.getFileUrl().isBlank()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(content.getFileUrl()))
                    .build();
        }

        if (content.getFileData() == null || content.getFileData().length == 0) {
            return ResponseEntity.notFound().build();
        }

        String fileName = content.getFileName() != null ? content.getFileName() : "content-file";
        String fileType = content.getFileType() != null
                ? content.getFileType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(fileType))
                .body(content.getFileData());
    }

    @GetMapping("/courses")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> getAssignedCourses() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getAssignedCourses())
                .build();
    }

    @GetMapping("/courses/{courseId}/curriculum")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> getCourseCurriculum(@PathVariable Long courseId) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getOrCreateCourseCurriculum(courseId))
                .build();
    }

    @PostMapping("/courses/{courseId}/curriculum")
    @PreAuthorize("hasRole('TRAINER')")
    public ApiResponse<?> saveCourseCurriculumDraft(
            @PathVariable Long courseId,
            @RequestBody Map<String, Object> payload
    ) throws Exception {
        Object jsonObject = payload.get("json");
        String jsonData = jsonObject instanceof String
                ? String.valueOf(jsonObject)
                : new ObjectMapper().writeValueAsString(jsonObject);

        return ApiResponse.builder()
                .success(true)
                .message("Curriculum draft saved successfully")
                .data(service.saveCourseCurriculumDraft(courseId, jsonData))
                .build();
    }
}