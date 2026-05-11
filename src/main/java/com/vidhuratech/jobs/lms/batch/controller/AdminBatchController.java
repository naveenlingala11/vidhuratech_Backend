package com.vidhuratech.jobs.lms.batch.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.lms.batch.dto.BatchCommunicationDto;
import com.vidhuratech.jobs.lms.batch.dto.BatchRequestDTO;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;
import com.vidhuratech.jobs.lms.batch.service.AdminBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lms/admin/batches")
@RequiredArgsConstructor
public class AdminBatchController {

    private final AdminBatchService service;
    private final BatchRepository batchRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ApiResponse<?> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long trainerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getAllBatches(keyword, status, courseId, trainerId, page, size))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ApiResponse<?> create(@RequestBody BatchRequestDTO dto) {
        return ApiResponse.builder()
                .success(true)
                .message("Batch created successfully")
                .data(service.createBatch(dto))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody BatchRequestDTO dto) {
        return ApiResponse.builder()
                .success(true)
                .message("Batch updated successfully")
                .data(service.updateBatch(id, dto))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public ApiResponse<?> delete(@PathVariable Long id) {
        service.deleteBatch(id);
        return ApiResponse.builder()
                .success(true)
                .message("Batch deleted successfully")
                .build();
    }

    // ✅ Corrected paths (relative to /api/lms/admin/batches)
    @GetMapping("/all-lite")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public List<Map<String, Object>> getAllBatchesLite() {
        return batchRepository.findAll().stream().map(b -> {
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("id", b.getId());
            row.put("name", b.getName() == null ? "" : b.getName());
            row.put("whatsappGroupLink", b.getWhatsappGroupLink() == null ? "" : b.getWhatsappGroupLink());
            row.put("zoomJoinLink", b.getZoomJoinLink() == null ? "" : b.getZoomJoinLink());
            row.put("zoomMeetingId", b.getZoomMeetingId() == null ? "" : b.getZoomMeetingId());
            row.put("zoomPasscode", b.getZoomPasscode() == null ? "" : b.getZoomPasscode());
            row.put("zoomSchedule", b.getZoomSchedule() == null ? "" : b.getZoomSchedule());
            row.put("zoomTime", b.getZoomTime() == null ? "" : b.getZoomTime());
            row.put("zoomCalendarLink", b.getZoomCalendarLink() == null ? "" : b.getZoomCalendarLink());
            return row;
        }).toList();
    }

    @GetMapping("/{id}/communication")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public BatchCommunicationDto getBatchCommunication(@PathVariable Long id) {
        Batch b = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found"));
        return toCommunicationDto(b);
    }

    @PutMapping("/{id}/communication")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','HR')")
    public BatchCommunicationDto updateCommunication(@PathVariable Long id,
                                                     @RequestBody BatchCommunicationDto dto) {
        Batch b = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        b.setWhatsappGroupLink(dto.getWhatsappGroupLink());
        b.setZoomJoinLink(dto.getZoomJoinLink());
        b.setZoomMeetingId(dto.getZoomMeetingId());
        b.setZoomPasscode(dto.getZoomPasscode());
        b.setZoomSchedule(dto.getZoomSchedule());
        b.setZoomTime(dto.getZoomTime());
        b.setZoomCalendarLink(dto.getZoomCalendarLink());

        Batch saved = batchRepository.save(b);
        return toCommunicationDto(saved);
    }

    private BatchCommunicationDto toCommunicationDto(Batch b) {
        BatchCommunicationDto dto = new BatchCommunicationDto();
        dto.setWhatsappGroupLink(b.getWhatsappGroupLink());
        dto.setZoomJoinLink(b.getZoomJoinLink());
        dto.setZoomMeetingId(b.getZoomMeetingId());
        dto.setZoomPasscode(b.getZoomPasscode());
        dto.setZoomSchedule(b.getZoomSchedule());
        dto.setZoomTime(b.getZoomTime());
        dto.setZoomCalendarLink(b.getZoomCalendarLink());
        return dto;
    }

}
