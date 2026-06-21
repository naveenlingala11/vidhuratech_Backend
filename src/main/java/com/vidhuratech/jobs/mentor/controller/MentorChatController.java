package com.vidhuratech.jobs.mentor.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.mentor.service.MentorChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentor-chat")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasAnyRole('STUDENT', 'MENTOR')")
public class MentorChatController {

    private final MentorChatService service;
    private final SecurityUtils securityUtils;

    @GetMapping("/{relationId}/messages")
    public ApiResponse<?> getMessages(@PathVariable Long relationId) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.getChatMessages(userId, relationId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{relationId}/messages")
    public ApiResponse<?> sendMessage(
            @PathVariable Long relationId,
            @RequestBody MessageRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.sendChatMessage(userId, relationId, request.getMessageText()));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @lombok.Data
    public static class MessageRequest {
        private String messageText;
    }
}
