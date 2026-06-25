package com.vidhuratech.jobs.publicpractice.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.publicpractice.service.PublicChallengeDiscussionService;
import com.vidhuratech.jobs.publicpractice.service.PublicPracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/practice")
@RequiredArgsConstructor
@CrossOrigin
public class PublicPracticeController {

    private final PublicPracticeService service;
    private final PublicChallengeDiscussionService discussionService;

    @GetMapping
    public ApiResponse<?> getPracticeLibrary(
            @RequestParam(required = false) String company
    ) {
        Object data = company == null || company.isBlank()
                ? service.getPracticeLibrary()
                : service.getPracticeLibraryByCompany(company);

        return ApiResponse.builder()
                .success(true)
                .data(data)
                .build();
    }

    @PostMapping("/lead")
    public ApiResponse<?> savePracticeLead(
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Registration completed successfully")
                .data(service.savePracticeLead(payload))
                .build();
    }

    @GetMapping("/assessments/{id}")
    public ApiResponse<?> getAssessment(@PathVariable Long id) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getPublicAssessment(id))
                .build();
    }

    @PostMapping("/assessments/{id}/submit")
    public ApiResponse<?> submitAssessment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Mock test submitted successfully")
                .data(service.submitPublicAssessment(id, payload))
                .build();
    }

    @GetMapping("/challenges/{id}")
    public ApiResponse<?> getChallenge(@PathVariable Long id) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getPublicChallenge(id))
                .build();
    }

    @PostMapping("/challenges/{id}/run")
    public ApiResponse<?> runChallenge(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Challenge evaluated successfully")
                .data(service.runPublicChallenge(id, payload))
                .build();
    }

    @PostMapping("/challenges/{id}/run-custom")
    public ApiResponse<?> runChallengeCustom(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Challenge custom run evaluated successfully")
                .data(service.runPublicChallengeCustom(id, payload))
                .build();
    }

    @PostMapping("/challenges/{id}/review")
    public ApiResponse<?> reviewChallenge(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        String code = String.valueOf(payload.getOrDefault("code", ""));
        String language = String.valueOf(payload.getOrDefault("language", ""));
        String accessToken = String.valueOf(payload.getOrDefault("accessToken", ""));

        return ApiResponse.builder()
                .success(true)
                .message("Code review generated successfully")
                .data(service.reviewPublicChallenge(id, code, language, accessToken))
                .build();
    }

    @PostMapping("/challenges/{id}/ai-hints")
    public ApiResponse<?> getChallengeAiHints(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        String accessToken = String.valueOf(payload.getOrDefault("accessToken", ""));
        return ApiResponse.builder()
                .success(true)
                .message("AI hints generated successfully")
                .data(service.getPublicChallengeAiHints(id, accessToken))
                .build();
    }

    @PostMapping("/access/register")
    public ApiResponse<?> registerAccess(
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Registration completed successfully")
                .data(service.registerPracticeAccess(payload))
                .build();
    }

    @GetMapping("/challenges/{id}/leaderboard")
    public ApiResponse<?> getChallengeLeaderboard(@PathVariable Long id) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getChallengeLeaderboard(id))
                .build();
    }

    @GetMapping("/announcements")
    public ApiResponse<?> getContestAnnouncements() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getContestAnnouncements())
                .build();
    }

    @GetMapping("/leaderboard/weekly")
    public ApiResponse<?> getWeeklyLeaderboard() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getCurrentWeeklyLeaderboard())
                .build();
    }

    @GetMapping("/leaderboard/daily")
    public ApiResponse<?> getDailyLeaderboard() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getCurrentDailyLeaderboard())
                .build();
    }

    @GetMapping("/leaderboard/monthly")
    public ApiResponse<?> getMonthlyLeaderboard() {
        return ApiResponse.builder()
                .success(true)
                .data(service.getCurrentMonthlyLeaderboard())
                .build();
    }

    @PostMapping("/access/session")
    public ApiResponse<?> registerAuthenticatedAccess(
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Practice access unlocked")
                .data(service.registerAuthenticatedPracticeAccess(payload))
                .build();
    }

    @PostMapping("/challenges/{id}/discussions")
    public ApiResponse<?> getChallengeDiscussions(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .data(discussionService.listDiscussions(id, payload == null ? Map.of() : payload))
                .build();
    }

    @PostMapping("/challenges/{id}/discussions/post")
    public ApiResponse<?> postChallengeDiscussion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Comment posted")
                .data(discussionService.postDiscussion(id, payload))
                .build();
    }

    @PostMapping("/challenges/{id}/discussions/{discussionId}/like")
    public ApiResponse<?> toggleChallengeDiscussionLike(
            @PathVariable Long id,
            @PathVariable Long discussionId,
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Like updated")
                .data(discussionService.toggleLike(id, discussionId, payload))
                .build();
    }

    @GetMapping("/challenges/{id}/best-submissions")
    public ApiResponse<?> getChallengeBestSubmissions(@PathVariable Long id) {
        return ApiResponse.builder()
                .success(true)
                .data(service.getChallengeBestSubmissions(id))
                .build();
    }

    @PostMapping("/challenges/{id}/discussions/{discussionId}/report")
    public ApiResponse<?> reportChallengeDiscussion(
            @PathVariable Long id,
            @PathVariable Long discussionId,
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("Comment reported")
                .data(discussionService.reportDiscussion(id, discussionId, payload))
                .build();
    }

    @PostMapping("/challenges/{id}/discussions/{discussionId}/block")
    public ApiResponse<?> blockChallengeDiscussionAuthor(
            @PathVariable Long id,
            @PathVariable Long discussionId,
            @RequestBody Map<String, Object> payload
    ) {
        return ApiResponse.builder()
                .success(true)
                .message("User blocked")
                .data(discussionService.blockDiscussionAuthor(id, discussionId, payload))
                .build();
    }
}