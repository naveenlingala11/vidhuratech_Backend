package com.vidhuratech.jobs.mentor.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.mentor.service.MentorQAService;
import com.vidhuratech.jobs.trainer.service.TrainerContentStorageService;
import com.vidhuratech.jobs.user.service.ReputationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
public class MentorQAController {

    private final MentorQAService service;
    private final SecurityUtils securityUtils;
    private final TrainerContentStorageService storageService;
    private final ReputationService reputationService;

    // --- Public endpoints ---
    @GetMapping("/api/public/qa")
    public ApiResponse<?> getQuestions(@RequestParam(required = false) String search) {
        try {
            Long userId = securityUtils.getCurrentUserId();
            return ApiResponse.success(service.getAllQuestions(userId, search));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/api/public/qa/{questionId}")
    public ApiResponse<?> getQuestionDetails(@PathVariable Long questionId) {
        try {
            Long userId = securityUtils.getCurrentUserId();
            // Increment view count on each detail load
            service.incrementViewCount(questionId);
            return ApiResponse.success(service.getQuestionDetails(userId, questionId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/api/public/qa/reputation/{userId}")
    public ApiResponse<?> getUserReputation(@PathVariable Long userId) {
        try {
            return ApiResponse.success(reputationService.getUserReputationDetails(userId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/api/public/qa/leaderboard")
    public ApiResponse<?> getLeaderboard() {
        try {
            return ApiResponse.success(service.getLeaderboard());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/api/public/qa/profile/{userId}")
    public ApiResponse<?> getUserProfile(@PathVariable Long userId) {
        try {
            return ApiResponse.success(service.getUserProfileDetails(userId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // --- Authenticated endpoints ---
    @PostMapping("/api/qa/questions")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> askQuestion(@RequestBody QuestionRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.createQuestion(
                    userId, 
                    request.getTitle(), 
                    request.getContent(), 
                    request.getTags(),
                    request.getMediaUrl(),
                    request.getMediaType(),
                    request.getPollOptions()
            ));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/api/qa/questions/{questionId}/like")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> toggleLikeQuestion(@PathVariable Long questionId) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.toggleLikeQuestion(userId, questionId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/api/qa/questions/{questionId}/answers")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> answerQuestion(
            @PathVariable Long questionId,
            @RequestBody AnswerRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.createAnswer(userId, questionId, request.getContent(), request.getParentAnswerId()));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/api/qa/answers/{answerId}/accept")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> acceptAnswer(@PathVariable Long answerId) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.acceptAnswer(userId, answerId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/api/qa/questions/{questionId}/solved")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> toggleSolvedQuestion(@PathVariable Long questionId) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.toggleSolvedQuestion(userId, questionId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/api/qa/upload")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> uploadMedia(@RequestParam("file") MultipartFile file) {
        try {
            if (file.getContentType() != null && file.getContentType().startsWith("video/") && file.getSize() > 10 * 1024 * 1024) {
                return ApiResponse.error("Video file size must be under 10MB");
            }
            String fileUrl = storageService.store(file);
            java.util.Map<String, String> res = new java.util.HashMap<>();
            res.put("url", fileUrl);
            return ApiResponse.success(res);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/api/public/qa/resolve-url")
    public ApiResponse<?> resolveUrl(@RequestParam("url") String url) {
        try {
            String resolved = service.resolveMediaUrl(url);
            java.util.Map<String, String> res = new java.util.HashMap<>();
            res.put("url", resolved);
            return ApiResponse.success(res);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/api/qa/questions/{questionId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody QuestionRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.updateQuestion(
                    userId, questionId,
                    request.getTitle(),
                    request.getContent(),
                    request.getTags()
            ));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/api/qa/answers/{answerId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> updateAnswer(
            @PathVariable Long answerId,
            @RequestBody AnswerRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.updateAnswer(userId, answerId, request.getContent()));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/api/qa/answers/{answerId}/vote")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> voteAnswer(
            @PathVariable Long answerId,
            @RequestBody VoteRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.voteAnswer(userId, answerId, request.getType()));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/api/qa/questions/{questionId}/pin")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> togglePinQuestion(@PathVariable Long questionId) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.togglePinQuestion(userId, questionId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/api/qa/answers/{answerId}/react")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> reactAnswer(
            @PathVariable Long answerId,
            @RequestBody ReactRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.toggleReaction(userId, answerId, request.getEmoji()));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/api/qa/questions/{questionId}/follow")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> toggleFollowQuestion(@PathVariable Long questionId) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.toggleFollowQuestion(userId, questionId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/api/qa/tags/follow")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> toggleFollowTag(@RequestBody TagFollowRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.toggleFollowTag(userId, request.getTag()));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/api/qa/following")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> getFollowedQuestions() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.getFollowedQuestions(userId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/api/qa/tags/following")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> getFollowedTags() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.getFollowedTags(userId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/api/qa/users/search")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> searchUsers(@RequestParam("q") String query) {
        try {
            return ApiResponse.success(service.searchUsers(query));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/api/qa/polls/{optionId}/vote")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> votePoll(@PathVariable Long optionId) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.votePoll(userId, optionId));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/api/qa/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> updateProfile(@RequestBody ProfileUpdateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        try {
            return ApiResponse.success(service.updateProfile(userId, request.getBio(), request.getSkills(), request.getSocialLinks()));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @lombok.Data
    public static class QuestionRequest {
        private String title;
        private String content;
        private String tags;
        private String mediaUrl;
        private String mediaType;
        private java.util.List<String> pollOptions;
    }

    @lombok.Data
    public static class AnswerRequest {
        private String content;
        private Long parentAnswerId;
    }

    @lombok.Data
    public static class VoteRequest {
        private String type; // "UP" or "DOWN"
    }

    @lombok.Data
    public static class ReactRequest {
        private String emoji;
    }

    @lombok.Data
    public static class TagFollowRequest {
        private String tag;
    }

    @lombok.Data
    public static class ProfileUpdateRequest {
        private String bio;
        private String skills;
        private String socialLinks;
    }
}
