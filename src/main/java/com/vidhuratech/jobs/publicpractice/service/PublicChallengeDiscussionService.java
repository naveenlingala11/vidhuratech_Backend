package com.vidhuratech.jobs.publicpractice.service;

import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.publicpractice.entity.PublicChallengeDiscussion;
import com.vidhuratech.jobs.publicpractice.entity.PublicChallengeDiscussionBlock;
import com.vidhuratech.jobs.publicpractice.entity.PublicChallengeDiscussionLike;
import com.vidhuratech.jobs.publicpractice.entity.PublicChallengeDiscussionReport;
import com.vidhuratech.jobs.publicpractice.repository.PublicChallengeDiscussionBlockRepository;
import com.vidhuratech.jobs.publicpractice.repository.PublicChallengeDiscussionLikeRepository;
import com.vidhuratech.jobs.publicpractice.repository.PublicChallengeDiscussionReportRepository;
import com.vidhuratech.jobs.publicpractice.repository.PublicChallengeDiscussionRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PublicChallengeDiscussionService {

    private final PublicChallengeDiscussionRepository discussionRepository;
    private final PublicChallengeDiscussionLikeRepository likeRepository;
    private final PublicChallengeDiscussionReportRepository reportRepository;
    private final PublicChallengeDiscussionBlockRepository blockRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public List<Map<String, Object>> listDiscussions(Long challengeId, Map<String, Object> payload) {
        String viewerKey = resolveViewerKey(payload, false);
        Set<String> blockedAuthors = blockedAuthors(viewerKey);

        return discussionRepository.findByChallengeIdAndParentIdIsNullOrderByCreatedAtDesc(challengeId)
                .stream()
                .filter(comment -> !blockedAuthors.contains(comment.getAuthorKey()))
                .map(comment -> toMap(comment, viewerKey, blockedAuthors))
                .toList();
    }

    public Map<String, Object> postDiscussion(Long challengeId, Map<String, Object> payload) {
        Actor actor = resolveActor(payload);

        String commentText = readText(payload, "comment");

        if (commentText.isBlank()) {
            throw new RuntimeException("Comment is required");
        }

        if (commentText.length() > 1200) {
            throw new RuntimeException("Comment should be below 1200 characters");
        }

        Long parentId = readLong(payload, "parentId");

        if (parentId != null) {
            PublicChallengeDiscussion parent = discussionRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));

            if (!Objects.equals(parent.getChallengeId(), challengeId)) {
                throw new RuntimeException("Parent comment not found");
            }

            if (parent.getParentId() != null) {
                parentId = parent.getParentId();
            }
        }

        PublicChallengeDiscussion discussion = new PublicChallengeDiscussion();
        discussion.setChallengeId(challengeId);
        discussion.setParentId(parentId);
        discussion.setAuthorName(actor.name());
        discussion.setAuthorEmail(actor.email());
        discussion.setAuthorKey(actor.key());
        discussion.setComment(commentText);
        discussion.setLikeCount(0);
        discussion.setReportCount(0);
        discussion.setCreatedAt(LocalDateTime.now());
        discussion.setUpdatedAt(LocalDateTime.now());

        return toMap(discussionRepository.save(discussion), actor.key(), Set.of());
    }

    public Map<String, Object> reportDiscussion(Long challengeId, Long discussionId, Map<String, Object> payload) {
        String reporterKey = resolveViewerKey(payload, true);

        PublicChallengeDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!Objects.equals(discussion.getChallengeId(), challengeId)) {
            throw new RuntimeException("Comment not found");
        }

        if (Objects.equals(discussion.getAuthorKey(), reporterKey)) {
            throw new RuntimeException("You cannot report your own comment");
        }

        Optional<PublicChallengeDiscussionReport> existing =
                reportRepository.findByDiscussionIdAndReporterKey(discussionId, reporterKey);

        if (existing.isEmpty()) {
            PublicChallengeDiscussionReport report = new PublicChallengeDiscussionReport();
            report.setDiscussionId(discussionId);
            report.setReporterKey(reporterKey);
            report.setReason(readText(payload, "reason"));
            report.setCreatedAt(LocalDateTime.now());
            reportRepository.save(report);
        }

        int count = Math.toIntExact(reportRepository.countByDiscussionId(discussionId));
        discussion.setReportCount(count);
        discussionRepository.save(discussion);

        Map<String, Object> response = toMap(discussion, reporterKey, blockedAuthors(reporterKey));
        response.put("reportedByMe", true);
        response.put("reportCount", count);

        return response;
    }

    public Map<String, Object> blockDiscussionAuthor(Long challengeId, Long discussionId, Map<String, Object> payload) {
        String blockerKey = resolveViewerKey(payload, true);

        PublicChallengeDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!Objects.equals(discussion.getChallengeId(), challengeId)) {
            throw new RuntimeException("Comment not found");
        }

        if (Objects.equals(discussion.getAuthorKey(), blockerKey)) {
            throw new RuntimeException("You cannot block yourself");
        }

        Optional<PublicChallengeDiscussionBlock> existing =
                blockRepository.findByBlockerKeyAndBlockedAuthorKey(blockerKey, discussion.getAuthorKey());

        if (existing.isEmpty()) {
            PublicChallengeDiscussionBlock block = new PublicChallengeDiscussionBlock();
            block.setBlockerKey(blockerKey);
            block.setBlockedAuthorKey(discussion.getAuthorKey());
            block.setBlockedAuthorName(safe(discussion.getAuthorName(), "Learner"));
            block.setCreatedAt(LocalDateTime.now());
            blockRepository.save(block);
        }

        return Map.of(
                "blocked", true,
                "blockedAuthorKey", discussion.getAuthorKey(),
                "blockedAuthorName", safe(discussion.getAuthorName(), "Learner")
        );
    }

    private Map<String, Object> toMap(
            PublicChallengeDiscussion comment,
            String viewerKey,
            Set<String> blockedAuthors
    ) {
        Map<String, Object> map = new LinkedHashMap<>();

        boolean likedByMe = viewerKey != null
                && likeRepository.findByDiscussionIdAndLikerKey(comment.getId(), viewerKey).isPresent();

        boolean reportedByMe = viewerKey != null
                && reportRepository.findByDiscussionIdAndReporterKey(comment.getId(), viewerKey).isPresent();

        List<Map<String, Object>> replies = discussionRepository.findByParentIdOrderByCreatedAtAsc(comment.getId())
                .stream()
                .filter(reply -> !blockedAuthors.contains(reply.getAuthorKey()))
                .map(reply -> toMap(reply, viewerKey, blockedAuthors))
                .toList();

        map.put("id", comment.getId());
        map.put("challengeId", comment.getChallengeId());
        map.put("parentId", comment.getParentId());
        map.put("authorName", safe(comment.getAuthorName(), "Learner"));
        map.put("authorKey", comment.getAuthorKey());
        map.put("comment", safe(comment.getComment(), ""));
        map.put("likeCount", comment.getLikeCount() == null ? 0 : comment.getLikeCount());
        map.put("reportCount", comment.getReportCount() == null ? 0 : comment.getReportCount());
        map.put("likedByMe", likedByMe);
        map.put("reportedByMe", reportedByMe);
        map.put("replyCount", replies.size());
        map.put("replies", replies);
        map.put("createdAt", comment.getCreatedAt());

        return map;
    }

    private Set<String> blockedAuthors(String viewerKey) {
        if (viewerKey == null || viewerKey.isBlank()) {
            return Set.of();
        }

        Set<String> blocked = new HashSet<>();

        for (PublicChallengeDiscussionBlock item : blockRepository.findByBlockerKey(viewerKey)) {
            if (item.getBlockedAuthorKey() != null && !item.getBlockedAuthorKey().isBlank()) {
                blocked.add(item.getBlockedAuthorKey());
            }
        }

        return blocked;
    }

    private Long readLong(Map<String, Object> payload, String key) {
        if (payload == null || payload.get(key) == null) {
            return null;
        }

        try {
            return Long.parseLong(String.valueOf(payload.get(key)));
        } catch (Exception ignored) {
            return null;
        }
    }

    public Map<String, Object> toggleLike(Long challengeId, Long discussionId, Map<String, Object> payload) {
        String likerKey = resolveViewerKey(payload, true);

        PublicChallengeDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!Objects.equals(discussion.getChallengeId(), challengeId)) {
            throw new RuntimeException("Comment not found");
        }

        Optional<PublicChallengeDiscussionLike> existing =
                likeRepository.findByDiscussionIdAndLikerKey(discussionId, likerKey);

        boolean liked;

        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            liked = false;
        } else {
            PublicChallengeDiscussionLike like = new PublicChallengeDiscussionLike();
            like.setDiscussionId(discussionId);
            like.setLikerKey(likerKey);
            like.setCreatedAt(LocalDateTime.now());
            likeRepository.save(like);
            liked = true;
        }

        int count = Math.toIntExact(likeRepository.countByDiscussionId(discussionId));
        discussion.setLikeCount(count);
        discussionRepository.save(discussion);

        Map<String, Object> response = toMap(discussion, likerKey, blockedAuthors(likerKey));
        response.put("likedByMe", liked);
        response.put("likeCount", count);

        return response;
    }

    private Map<String, Object> toMap(PublicChallengeDiscussion comment, String viewerKey) {
        Map<String, Object> map = new LinkedHashMap<>();

        boolean likedByMe = viewerKey != null
                && likeRepository.findByDiscussionIdAndLikerKey(comment.getId(), viewerKey).isPresent();

        map.put("id", comment.getId());
        map.put("challengeId", comment.getChallengeId());
        map.put("authorName", safe(comment.getAuthorName(), "Learner"));
        map.put("comment", safe(comment.getComment(), ""));
        map.put("likeCount", comment.getLikeCount() == null ? 0 : comment.getLikeCount());
        map.put("likedByMe", likedByMe);
        map.put("createdAt", comment.getCreatedAt());

        return map;
    }

    private Actor resolveActor(Map<String, Object> payload) {
        Long userId = securityUtils.getCurrentUserId();

        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);

            if (user != null) {
                String name = safe(user.getName(), user.getEmail());
                String email = safe(user.getEmail(), "");
                return new Actor("USER:" + user.getId(), name, email);
            }
        }

        String accessToken = readText(payload, "accessToken");

        if (accessToken.isBlank()) {
            throw new RuntimeException("Please register or login to discuss this challenge");
        }

        String name = readText(payload, "authorName");

        if (name.isBlank()) {
            name = "Learner";
        }

        String email = readText(payload, "authorEmail");

        return new Actor("GUEST:" + sha256(accessToken), name, email);
    }

    private String resolveViewerKey(Map<String, Object> payload, boolean required) {
        Long userId = securityUtils.getCurrentUserId();

        if (userId != null) {
            return "USER:" + userId;
        }

        String accessToken = readText(payload, "accessToken");

        if (!accessToken.isBlank()) {
            return "GUEST:" + sha256(accessToken);
        }

        if (required) {
            throw new RuntimeException("Please register or login to like comments");
        }

        return null;
    }

    private String readText(Map<String, Object> payload, String key) {
        if (payload == null) {
            return "";
        }

        Object value = payload.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder();

            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        } catch (Exception e) {
            return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8)).toString();
        }
    }

    private record Actor(String key, String name, String email) {
    }
}