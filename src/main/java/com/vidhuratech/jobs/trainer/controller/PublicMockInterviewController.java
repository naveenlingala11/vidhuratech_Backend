package com.vidhuratech.jobs.trainer.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.trainer.entity.MockInterviewRequest;
import com.vidhuratech.jobs.trainer.repository.MockInterviewRequestRepository;
import com.vidhuratech.jobs.trainer.service.TrainerWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/mock-interviews")
@CrossOrigin
public class PublicMockInterviewController {

    @Autowired
    @Lazy
    private MockInterviewRequestRepository mockRepository;

    @Autowired
    @Lazy
    private TrainerWorkflowService trainerWorkflowService;

    @GetMapping("/check/{id}")
    public ApiResponse<?> checkRoomStatus(@PathVariable Long id) {
        MockInterviewRequest request = mockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mock interview request not found"));

        Map<String, Object> map = new HashMap<>();
        map.put("id", request.getId());
        map.put("topic", request.getTopic() == null ? "Mock Interview" : request.getTopic());
        map.put("status", request.getStatus());
        map.put("expirationDate", request.getExpirationDate());
        map.put("maxDurationMinutes", request.getMaxDurationMinutes() == null ? 60 : request.getMaxDurationMinutes());
        map.put("isEnded", Boolean.TRUE.equals(request.getIsEnded()));
        map.put("trainerEmail", request.getTrainer() == null ? "" : request.getTrainer().getEmail());
        map.put("studentEmail", request.getStudent() == null ? "" : request.getStudent().getEmail());

        return ApiResponse.builder()
                .success(true)
                .data(map)
                .build();
    }

    @PostMapping("/create")
    public ApiResponse<?> createPublicSession(@RequestBody Map<String, Object> payload) {
        Map<String, Object> data = trainerWorkflowService.createPublicMockInterview(payload);
        return ApiResponse.builder()
                .success(true)
                .message("Session created successfully in database")
                .data(data)
                .build();
    }

    @PostMapping("/get-or-create")
    public ApiResponse<?> getOrCreatePublicSession(@RequestBody Map<String, Object> payload) {
        String roomName = payload.getOrDefault("roomName", "").toString().trim();
        String hostEmail = payload.getOrDefault("hostEmail", "").toString().trim();
        String hostName = payload.getOrDefault("hostName", "").toString().trim();

        if (roomName.isEmpty()) {
            throw new RuntimeException("Room name is required");
        }

        Long mockId = null;
        if (roomName.startsWith("VidhuraTech_Mock_Session_")) {
            try {
                mockId = Long.valueOf(roomName.substring("VidhuraTech_Mock_Session_".length()));
            } catch (Exception e) {}
        } else if (roomName.startsWith("VidhuraTech_Meeting_Session_")) {
            try {
                mockId = Long.valueOf(roomName.substring("VidhuraTech_Meeting_Session_".length()));
            } catch (Exception e) {}
        }

        if (mockId != null) {
            java.util.Optional<MockInterviewRequest> req = mockRepository.findById(mockId);
            if (req.isPresent()) {
                return ApiResponse.builder().success(true).data(trainerWorkflowService.mapMock(req.get())).build();
            }
        }

        java.util.List<MockInterviewRequest> existing = mockRepository.findAll().stream()
                .filter(r -> r.getMeetingLink() != null && r.getMeetingLink().contains(roomName))
                .toList();

        if (!existing.isEmpty()) {
            return ApiResponse.builder().success(true).data(trainerWorkflowService.mapMock(existing.get(0))).build();
        }

        Map<String, Object> createPayload = new HashMap<>();
        createPayload.put("topic", roomName.replace("_", " "));
        createPayload.put("hostEmail", hostEmail);
        createPayload.put("hostName", hostName);
        createPayload.put("candidateEmail", "");
        createPayload.put("candidateName", "Participant");
        createPayload.put("maxDurationMinutes", 60);
        createPayload.put("isPublic", true);

        Map<String, Object> data = trainerWorkflowService.createPublicMockInterview(createPayload);
        
        if (data.containsKey("id")) {
            Long newId = Long.valueOf(data.get("id").toString());
            mockRepository.findById(newId).ifPresent(req -> {
                req.setMeetingLink("/meeting/" + roomName);
                mockRepository.save(req);
            });
            data.put("meetingLink", "/meeting/" + roomName);
        }

        return ApiResponse.builder()
                .success(true)
                .message("Session retrieved or created successfully")
                .data(data)
                .build();
    }

    @PatchMapping("/{id}")
    public ApiResponse<?> updatePublicSession(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Map<String, Object> data = trainerWorkflowService.updatePublicMockInterviewTelemetry(id, payload);
        return ApiResponse.builder()
                .success(true)
                .message("Session telemetry updated")
                .data(data)
                .build();
    }

    @PostMapping("/{id}/join")
    public ApiResponse<?> logJoinEvent(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String name = payload.getOrDefault("name", "Guest").toString();
        String email = payload.getOrDefault("email", "").toString();
        String role = payload.getOrDefault("role", "GUEST").toString();
        
        trainerWorkflowService.logMeetingJoin(id, name, email, role);
        return ApiResponse.builder()
                .success(true)
                .message("Join event logged successfully")
                .build();
    }
}
