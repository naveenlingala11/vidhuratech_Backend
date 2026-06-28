package com.vidhuratech.jobs.trainer.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.trainer.entity.MockInterviewRequest;
import com.vidhuratech.jobs.trainer.repository.MockInterviewRequestRepository;
import com.vidhuratech.jobs.trainer.service.TrainerWorkflowService;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mock-interviews")
@CrossOrigin
public class MockInterviewController {

    @Autowired
    @Lazy
    private MockInterviewRequestRepository mockRepository;

    @Autowired
    @Lazy
    private TrainerWorkflowService trainerWorkflowService;

    @Autowired
    @Lazy
    private SecurityUtils securityUtils;

    @GetMapping("/my-sessions")
    public ApiResponse<?> getMySessions() {
        String email = securityUtils.getCurrentUserEmail();
        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            throw new RuntimeException("Authentication required. Please log in first.");
        }
        return ApiResponse.builder()
                .success(true)
                .data(trainerWorkflowService.getMySessions(email))
                .build();
    }

    @PutMapping("/{id}/edit")
    public ApiResponse<?> editPublicSession(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> payload
    ) {
        String email = securityUtils.getCurrentUserEmail();
        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            throw new RuntimeException("Authentication required. Please log in first.");
        }
        Map<String, Object> data = trainerWorkflowService.updatePublicMockInterview(id, payload, email);
        return ApiResponse.builder()
                .success(true)
                .message("Session updated successfully")
                .data(data)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deletePublicSession(@PathVariable Long id) {
        String email = securityUtils.getCurrentUserEmail();
        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            throw new RuntimeException("Authentication required. Please log in first.");
        }
        
        MockInterviewRequest request = mockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mock interview request not found"));
                
        String hostEmail = request.getHostEmail();
        String trainerEmail = request.getTrainer() != null ? request.getTrainer().getEmail() : null;

        boolean isHost = email.equalsIgnoreCase(hostEmail)
                || (trainerEmail != null && email.equalsIgnoreCase(trainerEmail));

        if (!isHost) {
            throw new RuntimeException("Access denied. Only the session host can delete this session.");
        }
        
        mockRepository.delete(request);
        return ApiResponse.builder()
                .success(true)
                .message("Session deleted/cancelled successfully")
                .build();
    }
}
