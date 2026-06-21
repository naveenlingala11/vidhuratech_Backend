package com.vidhuratech.jobs.mentor.service;

import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.mentor.entity.MentorAnswer;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.mentor.entity.MentorAnswerVote;
import com.vidhuratech.jobs.mentor.entity.MentorQuestion;
import com.vidhuratech.jobs.mentor.entity.MentorQuestionLike;
import com.vidhuratech.jobs.mentor.repository.MentorAnswerRepository;
import com.vidhuratech.jobs.mentor.repository.MentorAnswerVoteRepository;
import com.vidhuratech.jobs.mentor.repository.MentorQuestionRepository;
import com.vidhuratech.jobs.mentor.repository.MentorQuestionLikeRepository;
import com.vidhuratech.jobs.mentor.entity.MentorAnswerReaction;
import com.vidhuratech.jobs.mentor.entity.MentorQuestionFollow;
import com.vidhuratech.jobs.mentor.entity.MentorTagFollow;
import com.vidhuratech.jobs.mentor.repository.MentorAnswerReactionRepository;
import com.vidhuratech.jobs.mentor.repository.MentorQuestionFollowRepository;
import com.vidhuratech.jobs.mentor.repository.MentorTagFollowRepository;
import com.vidhuratech.jobs.mentor.entity.MentorPoll;
import com.vidhuratech.jobs.mentor.entity.MentorPollVote;
import com.vidhuratech.jobs.mentor.repository.MentorPollRepository;
import com.vidhuratech.jobs.mentor.repository.MentorPollVoteRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import com.vidhuratech.jobs.user.service.ReputationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorQAService {

    private final MentorQuestionRepository questionRepo;
    private final MentorAnswerRepository answerRepo;
    private final UserRepository userRepo;
    private final MentorQuestionLikeRepository likeRepo;
    private final MentorAnswerVoteRepository voteRepo;
    private final ReputationService reputationService;
    private final MentorAnswerReactionRepository reactionRepo;
    private final MentorQuestionFollowRepository questionFollowRepo;
    private final MentorTagFollowRepository tagFollowRepo;
    private final ActivityNotificationService notificationService;
    private final MentorPollRepository pollRepo;
    private final MentorPollVoteRepository pollVoteRepo;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllQuestions(Long currentUserId, String search) {
        List<MentorQuestion> questions;
        if (search != null && !search.trim().isEmpty()) {
            questions = questionRepo.searchQuestions(search.trim());
        } else {
            questions = questionRepo.findAllWithAuthor();
        }

        return questions.stream().map(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("title", q.getTitle());
            map.put("content", q.getContent());
            map.put("tags", q.getTags());
            map.put("mediaUrl", q.getMediaUrl());
            map.put("mediaType", q.getMediaType());
            map.put("isSolved", q.getIsSolved());
            map.put("createdAt", q.getCreatedAt());
            map.put("updatedAt", q.getUpdatedAt());
            map.put("isEdited", q.getIsEdited());
            map.put("editCount", q.getEditCount());
            map.put("authorId", q.getAuthor().getId());
            map.put("authorName", q.getAuthor().getName());
            map.put("authorAvatar", q.getAuthor().getName() != null && !q.getAuthor().getName().isEmpty()
                    ? q.getAuthor().getName().substring(0, 1).toUpperCase() : "U");
            map.put("authorProfileImageUrl", q.getAuthor().getProfileImageUrl());
            map.put("answersCount", q.getAnswers().size());
            map.put("isPinned", q.getIsPinned());
            map.put("viewsCount", q.getViewsCount() != null ? q.getViewsCount() : 0);
            map.put("authorReputation", q.getAuthor().getReputationPoints() != null ? q.getAuthor().getReputationPoints() : 0);
            map.put("authorLevel", q.getAuthor().getReputationLevel() != null ? q.getAuthor().getReputationLevel() : "BEGINNER");

            boolean isFollowing = false;
            if (currentUserId != null) {
                isFollowing = questionFollowRepo.existsByQuestionIdAndUserId(q.getId(), currentUserId);
            }
            map.put("isFollowing", isFollowing);

            // Compute last activity: latest among question update and newest answer
            java.time.LocalDateTime lastActivity = q.getUpdatedAt() != null ? q.getUpdatedAt() : q.getCreatedAt();
            if (!q.getAnswers().isEmpty()) {
                java.time.LocalDateTime latestAnswer = q.getAnswers().stream()
                        .map(a -> a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getCreatedAt())
                        .max(java.time.LocalDateTime::compareTo).orElse(lastActivity);
                if (latestAnswer.isAfter(lastActivity)) lastActivity = latestAnswer;
            }
            map.put("lastActivity", lastActivity);
            
            long likesCount = likeRepo.countByQuestionId(q.getId());
            map.put("likesCount", likesCount);
            
            boolean isLikedByMe = false;
            if (currentUserId != null) {
                isLikedByMe = likeRepo.existsByQuestionIdAndUserId(q.getId(), currentUserId);
            }
            map.put("isLikedByMe", isLikedByMe);
            
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getQuestionDetails(Long currentUserId, Long questionId) {
        MentorQuestion q = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("id", q.getId());
        result.put("title", q.getTitle());
        result.put("content", q.getContent());
        result.put("tags", q.getTags());
        result.put("mediaUrl", q.getMediaUrl());
        result.put("mediaType", q.getMediaType());
        result.put("isSolved", q.getIsSolved());
        result.put("createdAt", q.getCreatedAt());
        result.put("updatedAt", q.getUpdatedAt());
        result.put("isEdited", q.getIsEdited());
        result.put("editCount", q.getEditCount());
        result.put("isPinned", q.getIsPinned());
        result.put("viewsCount", q.getViewsCount() != null ? q.getViewsCount() : 0);
        result.put("authorId", q.getAuthor().getId());
        result.put("authorName", q.getAuthor().getName());
        result.put("authorAvatar", q.getAuthor().getName() != null && !q.getAuthor().getName().isEmpty()
                ? q.getAuthor().getName().substring(0, 1).toUpperCase() : "U");
        result.put("authorProfileImageUrl", q.getAuthor().getProfileImageUrl());
        result.put("authorReputation", q.getAuthor().getReputationPoints() != null ? q.getAuthor().getReputationPoints() : 0);
        result.put("authorLevel", q.getAuthor().getReputationLevel() != null ? q.getAuthor().getReputationLevel() : "BEGINNER");

        boolean isFollowing = false;
        if (currentUserId != null) {
            isFollowing = questionFollowRepo.existsByQuestionIdAndUserId(q.getId(), currentUserId);
        }
        result.put("isFollowing", isFollowing);

        long likesCount = likeRepo.countByQuestionId(q.getId());
        result.put("likesCount", likesCount);
        
        boolean isLikedByMe = false;
        if (currentUserId != null) {
            isLikedByMe = likeRepo.existsByQuestionIdAndUserId(q.getId(), currentUserId);
        }
        result.put("isLikedByMe", isLikedByMe);

        List<MentorAnswer> answers = answerRepo.findAllByQuestionIdWithAuthor(questionId);
        // Batch load votes and reactions for all answers
        List<Long> answerIds = answers.stream().map(MentorAnswer::getId).collect(Collectors.toList());
        List<MentorAnswerVote> allVotes = answerIds.isEmpty() ? Collections.emptyList() : voteRepo.findAllByAnswerIdIn(answerIds);
        Map<Long, List<MentorAnswerVote>> votesByAnswer = allVotes.stream().collect(Collectors.groupingBy(v -> v.getAnswer().getId()));

        List<MentorAnswerReaction> allReactions = answerIds.isEmpty() ? Collections.emptyList() : reactionRepo.findAllByAnswerIdIn(answerIds);
        Map<Long, List<MentorAnswerReaction>> reactionsByAnswer = allReactions.stream().collect(Collectors.groupingBy(r -> r.getAnswer().getId()));

        // Filter out root answers (parentAnswer == null)
        List<MentorAnswer> rootAnswers = answers.stream()
                .filter(a -> a.getParentAnswer() == null)
                .collect(Collectors.toList());

        List<Map<String, Object>> mappedAnswers = rootAnswers.stream().map(a ->
            mapAnswerToHierarchy(a, answers, currentUserId, votesByAnswer, reactionsByAnswer)
        ).collect(Collectors.toList());

        // Compute last activity: latest among question update and newest answer
        java.time.LocalDateTime lastActivity = q.getUpdatedAt() != null ? q.getUpdatedAt() : q.getCreatedAt();
        if (!answers.isEmpty()) {
            java.time.LocalDateTime latestAnswer = answers.stream()
                    .map(a -> a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getCreatedAt())
                    .max(java.time.LocalDateTime::compareTo).orElse(lastActivity);
            if (latestAnswer.isAfter(lastActivity)) lastActivity = latestAnswer;
        }
        result.put("lastActivity", lastActivity);

        // Poll details
        List<MentorPoll> polls = pollRepo.findAllByQuestionId(questionId);
        if (!polls.isEmpty()) {
            List<Map<String, Object>> pollOptionMaps = new ArrayList<>();
            boolean hasVoted = false;
            Long votedOptionId = null;
            long totalPollVotes = 0;

            if (currentUserId != null) {
                List<MentorPollVote> userVotes = pollVoteRepo.findAllByPollQuestionIdAndUserId(questionId, currentUserId);
                if (!userVotes.isEmpty()) {
                    hasVoted = true;
                    votedOptionId = userVotes.get(0).getPoll().getId();
                }
            }

            for (MentorPoll p : polls) {
                Map<String, Object> pm = new HashMap<>();
                pm.put("id", p.getId());
                pm.put("optionText", p.getOptionText());
                pm.put("votesCount", p.getVotesCount() != null ? p.getVotesCount() : 0);
                totalPollVotes += (p.getVotesCount() != null ? p.getVotesCount() : 0);
                pollOptionMaps.add(pm);
            }

            Map<String, Object> pollData = new HashMap<>();
            pollData.put("options", pollOptionMaps);
            pollData.put("totalVotes", totalPollVotes);
            pollData.put("hasVoted", hasVoted);
            pollData.put("votedOptionId", votedOptionId);
            result.put("poll", pollData);
        } else {
            result.put("poll", null);
        }

        result.put("answers", mappedAnswers);
        return result;
    }

    @Transactional
    public Map<String, Object> createQuestion(Long authorId, String title, String content, String tags, String mediaUrl, String mediaType, List<String> pollOptions) {
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("Question title is required");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Question description is required");
        }

        User author = userRepo.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author user not found"));

        MentorQuestion q = new MentorQuestion();
        q.setAuthor(author);
        q.setTitle(title.trim());
        q.setContent(content.trim());
        q.setTags(tags != null ? tags.trim() : null);
        q.setMediaUrl(mediaUrl != null && !mediaUrl.trim().isEmpty() ? mediaUrl.trim() : null);
        q.setMediaType(mediaType != null && !mediaType.trim().isEmpty() ? mediaType.trim() : "NONE");

        questionRepo.save(q);

        // If poll options exist, save them
        if (pollOptions != null && !pollOptions.isEmpty()) {
            for (String option : pollOptions) {
                if (option != null && !option.trim().isEmpty()) {
                    MentorPoll poll = new MentorPoll();
                    poll.setQuestion(q);
                    poll.setOptionText(option.trim());
                    poll.setVotesCount(0);
                    pollRepo.save(poll);
                }
            }
        }

        // Award reputation points for asking a question
        reputationService.awardPoints(authorId, 5, "Asked a question", "QUESTION", q.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Question posted successfully");
        result.put("questionId", q.getId());
        return result;
    }

    @Transactional
    public Map<String, Object> toggleSolvedQuestion(Long userId, Long questionId) {
        MentorQuestion q = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAuthor = q.getAuthor().getId().equals(userId);
        boolean isMentorOrAdmin = "MENTOR".equals(user.getRole().name()) || "ADMIN".equals(user.getRole().name());

        if (!isAuthor && !isMentorOrAdmin) {
            throw new RuntimeException("Unauthorized: Only the author or a mentor/admin can toggle solved status");
        }

        q.setIsSolved(!q.getIsSolved());
        questionRepo.save(q);

        Map<String, Object> result = new HashMap<>();
        result.put("solved", q.getIsSolved());
        return result;
    }

    @Transactional
    public Map<String, Object> toggleLikeQuestion(Long userId, Long questionId) {
        MentorQuestion q = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<MentorQuestionLike> existing = likeRepo.findByQuestionIdAndUserId(questionId, userId);
        boolean liked;
        if (existing.isPresent()) {
            likeRepo.delete(existing.get());
            liked = false;
        } else {
            MentorQuestionLike like = new MentorQuestionLike();
            like.setQuestion(q);
            like.setUser(user);
            likeRepo.save(like);
            liked = true;
        }

        long count = likeRepo.countByQuestionId(questionId);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likesCount", count);
        return result;
    }

    @Transactional
    public Map<String, Object> createAnswer(Long authorId, Long questionId, String content, Long parentAnswerId) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Answer content is required");
        }

        MentorQuestion q = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        User author = userRepo.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author user not found"));

        MentorAnswer a = new MentorAnswer();
        a.setQuestion(q);
        a.setAuthor(author);
        a.setContent(content.trim());
        a.setIsAccepted(false);

        if (parentAnswerId != null) {
            MentorAnswer parent = answerRepo.findById(parentAnswerId)
                    .orElseThrow(() -> new RuntimeException("Parent answer not found: " + parentAnswerId));
            a.setParentAnswer(parent);
            a.setDepth((parent.getDepth() != null ? parent.getDepth() : 0) + 1);
        } else {
            a.setDepth(0);
        }

        answerRepo.save(a);

        // Award reputation points for posting an answer
        reputationService.awardPoints(authorId, 10, "Answered a question", "ANSWER", a.getId());

        // Parse and notify mentions
        parseAndNotifyMentions(content, a);

        // Notify question author
        if (!q.getAuthor().getId().equals(authorId)) {
            notificationService.notifyUserInAppOnly(
                q.getAuthor(),
                "New reply to your discussion",
                author.getName() + " replied to your topic: '" + q.getTitle() + "'",
                "QA_REPLY",
                "/qa/" + q.getId()
            );
        }

        // Notify thread followers
        List<MentorQuestionFollow> follows = questionFollowRepo.findAllByQuestionId(q.getId());
        for (MentorQuestionFollow f : follows) {
            if (!f.getUser().getId().equals(authorId) && !f.getUser().getId().equals(q.getAuthor().getId())) {
                notificationService.notifyUserInAppOnly(
                    f.getUser(),
                    "Update on discussion you follow",
                    "New reply posted on '" + q.getTitle() + "'",
                    "QA_FOLLOW",
                    "/qa/" + q.getId()
                );
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Answer submitted successfully");
        result.put("answerId", a.getId());
        return result;
    }

    @Transactional
    public Map<String, Object> acceptAnswer(Long userId, Long answerId) {
        MentorAnswer answer = answerRepo.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        MentorQuestion q = answer.getQuestion();
        if (!q.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only the creator of the question can accept an answer");
        }

        List<MentorAnswer> all = answerRepo.findAllByQuestionIdWithAuthor(q.getId());
        for (MentorAnswer a : all) {
            boolean wasAccepted = Boolean.TRUE.equals(a.getIsAccepted());
            boolean isNowAccepted = a.getId().equals(answerId);

            if (isNowAccepted && !wasAccepted) {
                a.setIsAccepted(true);
                answerRepo.save(a);
                reputationService.awardPoints(a.getAuthor().getId(), 25, "Answer accepted", "ACCEPT", a.getId());

                // Notify answer author
                if (!a.getAuthor().getId().equals(userId)) {
                    notificationService.notifyUserInAppOnly(
                        a.getAuthor(),
                        "Your answer was accepted!",
                        "Your reply on '" + q.getTitle() + "' was accepted by the author",
                        "QA_ACCEPT",
                        "/qa/" + q.getId()
                    );
                }
            } else if (!isNowAccepted && wasAccepted) {
                a.setIsAccepted(false);
                answerRepo.save(a);
                reputationService.awardPoints(a.getAuthor().getId(), -25, "Answer unaccepted", "UNACCEPT", a.getId());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Answer marked as accepted");
        return result;
    }

    @Transactional
    public Map<String, Object> updateQuestion(Long userId, Long questionId, String title, String content, String tags) {
        MentorQuestion q = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (!q.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only the post author can edit this discussion");
        }

        if (title != null && !title.trim().isEmpty()) {
            q.setTitle(title.trim());
        }
        if (content != null && !content.trim().isEmpty()) {
            q.setContent(content.trim());
        }
        q.setTags(tags != null ? tags.trim() : q.getTags());
        q.setIsEdited(true);
        q.setEditCount(q.getEditCount() != null ? q.getEditCount() + 1 : 1);

        questionRepo.save(q);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Question updated successfully");
        result.put("questionId", q.getId());
        result.put("updatedAt", q.getUpdatedAt());
        result.put("editCount", q.getEditCount());
        return result;
    }

    @Transactional
    public Map<String, Object> updateAnswer(Long userId, Long answerId, String content) {
        MentorAnswer a = answerRepo.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        if (!a.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Only the comment author can edit this reply");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Comment content cannot be empty");
        }

        a.setContent(content.trim());
        a.setIsEdited(true);
        answerRepo.save(a);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Comment updated successfully");
        result.put("answerId", a.getId());
        result.put("updatedAt", a.getUpdatedAt());
        return result;
    }

    @Transactional
    public Map<String, Object> voteAnswer(Long userId, Long answerId, String voteType) {
        if (!"UP".equals(voteType) && !"DOWN".equals(voteType)) {
            throw new RuntimeException("Invalid vote type. Must be 'UP' or 'DOWN'");
        }

        MentorAnswer answer = answerRepo.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<MentorAnswerVote> existing = voteRepo.findByAnswerIdAndUserId(answerId, userId);

        String resultVote = null;
        if (existing.isPresent()) {
            MentorAnswerVote vote = existing.get();
            String oldVoteType = vote.getVoteType();
            if (oldVoteType.equals(voteType)) {
                // Same vote again → remove (toggle off)
                voteRepo.delete(vote);
                resultVote = null;

                // Revert points
                if ("UP".equals(oldVoteType)) {
                    reputationService.awardPoints(answer.getAuthor().getId(), -5, "Answer upvote removed", "VOTE_REVERT", answer.getId());
                } else {
                    reputationService.awardPoints(answer.getAuthor().getId(), 2, "Answer downvote removed", "VOTE_REVERT", answer.getId());
                    reputationService.awardPoints(userId, 1, "Downvote cost refunded", "VOTE_REVERT", answer.getId());
                }
            } else {
                // Switch vote direction
                vote.setVoteType(voteType);
                voteRepo.save(vote);
                resultVote = voteType;

                // Revert old points and apply new points
                if ("UP".equals(oldVoteType)) { // UP -> DOWN
                    reputationService.awardPoints(answer.getAuthor().getId(), -7, "Vote changed from UP to DOWN", "VOTE_SWITCH", answer.getId());
                    reputationService.awardPoints(userId, -1, "Downvoted answer", "VOTE", answer.getId());
                } else { // DOWN -> UP
                    reputationService.awardPoints(answer.getAuthor().getId(), 7, "Vote changed from DOWN to UP", "VOTE_SWITCH", answer.getId());
                    reputationService.awardPoints(userId, 1, "Downvote cost refunded", "VOTE", answer.getId());

                    // Notify answer author
                    if (!answer.getAuthor().getId().equals(userId)) {
                        notificationService.notifyUserInAppOnly(
                            answer.getAuthor(),
                            "Your answer received an upvote!",
                            "A user upvoted your response on '" + answer.getQuestion().getTitle() + "'",
                            "QA_VOTE",
                            "/qa/" + answer.getQuestion().getId()
                        );
                    }
                }
            }
        } else {
            // New vote
            MentorAnswerVote vote = new MentorAnswerVote();
            vote.setAnswer(answer);
            vote.setUser(user);
            vote.setVoteType(voteType);
            voteRepo.save(vote);
            resultVote = voteType;

            // Apply points
            if ("UP".equals(voteType)) {
                reputationService.awardPoints(answer.getAuthor().getId(), 5, "Answer upvoted", "VOTE", answer.getId());

                // Notify answer author
                if (!answer.getAuthor().getId().equals(userId)) {
                    notificationService.notifyUserInAppOnly(
                        answer.getAuthor(),
                        "Your answer received an upvote!",
                        "A user upvoted your response on '" + answer.getQuestion().getTitle() + "'",
                        "QA_VOTE",
                        "/qa/" + answer.getQuestion().getId()
                    );
                }
            } else {
                reputationService.awardPoints(answer.getAuthor().getId(), -2, "Answer downvoted", "VOTE", answer.getId());
                reputationService.awardPoints(userId, -1, "Downvoted answer", "VOTE", answer.getId());
            }
        }

        long upvotes = voteRepo.countUpvotesByAnswerId(answerId);
        long downvotes = voteRepo.countDownvotesByAnswerId(answerId);

        Map<String, Object> result = new HashMap<>();
        result.put("userVote", resultVote);
        result.put("votesScore", upvotes - downvotes);
        return result;
    }

    @Transactional
    public Map<String, Object> togglePinQuestion(Long userId, Long questionId) {
        MentorQuestion q = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isMentorOrAdmin = "MENTOR".equals(user.getRole().name()) || "ADMIN".equals(user.getRole().name());
        if (!isMentorOrAdmin) {
            throw new RuntimeException("Unauthorized: Only mentors or admins can pin discussions");
        }

        q.setIsPinned(!q.getIsPinned());
        questionRepo.save(q);

        Map<String, Object> result = new HashMap<>();
        result.put("pinned", q.getIsPinned());
        return result;
    }

    @Transactional
    public void incrementViewCount(Long questionId) {
        questionRepo.findById(questionId).ifPresent(q -> {
            q.setViewsCount(q.getViewsCount() != null ? q.getViewsCount() + 1 : 1);
            questionRepo.save(q);
        });
    }

    @Transactional
    public Map<String, Object> toggleReaction(Long userId, Long answerId, String emoji) {
        MentorAnswer answer = answerRepo.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<MentorAnswerReaction> existing = reactionRepo.findByAnswerIdAndUserIdAndEmoji(answerId, userId, emoji);
        if (existing.isPresent()) {
            reactionRepo.delete(existing.get());
        } else {
            MentorAnswerReaction reaction = new MentorAnswerReaction();
            reaction.setAnswer(answer);
            reaction.setUser(user);
            reaction.setEmoji(emoji);
            reactionRepo.save(reaction);
        }

        List<MentorAnswerReaction> reactions = reactionRepo.findAllByAnswerId(answerId);
        Map<String, Long> reactionCounts = reactions.stream()
                .collect(Collectors.groupingBy(MentorAnswerReaction::getEmoji, Collectors.counting()));
        
        List<String> userReactions = reactions.stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .map(MentorAnswerReaction::getEmoji)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("reactionCounts", reactionCounts);
        result.put("userReactions", userReactions);
        return result;
    }

    @Transactional
    public Map<String, Object> toggleFollowQuestion(Long userId, Long questionId) {
        MentorQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<MentorQuestionFollow> existing = questionFollowRepo.findByQuestionIdAndUserId(questionId, userId);
        boolean followed;
        if (existing.isPresent()) {
            questionFollowRepo.delete(existing.get());
            followed = false;
        } else {
            MentorQuestionFollow follow = new MentorQuestionFollow();
            follow.setQuestion(question);
            follow.setUser(user);
            questionFollowRepo.save(follow);
            followed = true;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("followed", followed);
        return result;
    }

    @Transactional
    public Map<String, Object> toggleFollowTag(Long userId, String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            throw new RuntimeException("Tag name is required");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<MentorTagFollow> existing = tagFollowRepo.findByUserIdAndTagName(userId, tagName.trim());
        boolean followed;
        if (existing.isPresent()) {
            tagFollowRepo.delete(existing.get());
            followed = false;
        } else {
            MentorTagFollow follow = new MentorTagFollow();
            follow.setUser(user);
            follow.setTagName(tagName.trim());
            tagFollowRepo.save(follow);
            followed = true;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("followed", followed);
        return result;
    }

    @Transactional(readOnly = true)
    public List<String> getFollowedTags(Long userId) {
        List<MentorTagFollow> follows = tagFollowRepo.findAllByUserId(userId);
        return follows.stream()
                .map(MentorTagFollow::getTagName)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getFollowedQuestions(Long userId) {
        List<MentorQuestionFollow> follows = questionFollowRepo.findAllByUserId(userId);
        List<MentorQuestion> questions = follows.stream()
                .map(MentorQuestionFollow::getQuestion)
                .collect(Collectors.toList());

        return questions.stream().map(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("title", q.getTitle());
            map.put("content", q.getContent());
            map.put("tags", q.getTags());
            map.put("mediaUrl", q.getMediaUrl());
            map.put("mediaType", q.getMediaType());
            map.put("isSolved", q.getIsSolved());
            map.put("createdAt", q.getCreatedAt());
            map.put("updatedAt", q.getUpdatedAt());
            map.put("isEdited", q.getIsEdited());
            map.put("editCount", q.getEditCount());
            map.put("authorId", q.getAuthor().getId());
            map.put("authorName", q.getAuthor().getName());
            map.put("authorAvatar", q.getAuthor().getName() != null && !q.getAuthor().getName().isEmpty()
                    ? q.getAuthor().getName().substring(0, 1).toUpperCase() : "U");
            map.put("authorProfileImageUrl", q.getAuthor().getProfileImageUrl());
            map.put("authorReputation", q.getAuthor().getReputationPoints() != null ? q.getAuthor().getReputationPoints() : 0);
            map.put("authorLevel", q.getAuthor().getReputationLevel() != null ? q.getAuthor().getReputationLevel() : "BEGINNER");
            map.put("answersCount", q.getAnswers().size());
            map.put("isPinned", q.getIsPinned());
            map.put("viewsCount", q.getViewsCount() != null ? q.getViewsCount() : 0);
            map.put("isFollowing", true);
            return map;
        }).collect(Collectors.toList());
    }

    public String resolveMediaUrl(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return urlString;
        }
        try {
            if (urlString.contains("imgurl=")) {
                String decoded = extractQueryParam(urlString, "imgurl");
                if (decoded != null) return decoded;
            }

            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(urlString).openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int status = conn.getResponseCode();
            String finalUrl = conn.getURL().toString();
            
            if (finalUrl.contains("imgurl=")) {
                String decoded = extractQueryParam(finalUrl, "imgurl");
                if (decoded != null) return decoded;
            }

            String contentType = conn.getContentType();
            if (contentType != null && contentType.contains("text/html")) {
                java.io.BufferedReader in = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder html = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    html.append(inputLine);
                }
                in.close();
                
                String htmlStr = html.toString();
                java.util.regex.Pattern ogPattern = java.util.regex.Pattern.compile("<meta[^>]+property=\"og:image\"[^>]+content=\"([^\"]+)\"");
                java.util.regex.Matcher m = ogPattern.matcher(htmlStr);
                if (m.find()) {
                    return m.group(1).replace("&amp;", "&");
                }
                
                java.util.regex.Pattern ogPatternRev = java.util.regex.Pattern.compile("<meta[^>]+content=\"([^\"]+)\"[^>]+property=\"og:image\"");
                m = ogPatternRev.matcher(htmlStr);
                if (m.find()) {
                    return m.group(1).replace("&amp;", "&");
                }

                java.util.regex.Pattern twitterPattern = java.util.regex.Pattern.compile("<meta[^>]+name=\"twitter:image\"[^>]+content=\"([^\"]+)\"");
                m = twitterPattern.matcher(htmlStr);
                if (m.find()) {
                    return m.group(1).replace("&amp;", "&");
                }
                
                java.util.regex.Pattern twitterPatternRev = java.util.regex.Pattern.compile("<meta[^>]+content=\"([^\"]+)\"[^>]+name=\"twitter:image\"");
                m = twitterPatternRev.matcher(htmlStr);
                if (m.find()) {
                    return m.group(1).replace("&amp;", "&");
                }
            }

            return finalUrl;
        } catch (Exception e) {
            System.err.println("Error resolving media URL: " + e.getMessage());
            return urlString;
        }
    }

    private String extractQueryParam(String url, String param) {
        try {
            int qIndex = url.indexOf('?');
            if (qIndex == -1) return null;
            String query = url.substring(qIndex + 1);
            for (String pair : query.split("&")) {
                int eq = pair.indexOf('=');
                if (eq != -1) {
                    String name = pair.substring(0, eq);
                    if (name.equalsIgnoreCase(param)) {
                        return java.net.URLDecoder.decode(pair.substring(eq + 1), "UTF-8");
                    }
                }
            }
        } catch (Exception e) {}
        return null;
    }

    private Map<String, Object> mapAnswerToHierarchy(
            MentorAnswer a,
            List<MentorAnswer> allAnswers,
            Long currentUserId,
            Map<Long, List<MentorAnswerVote>> votesByAnswer,
            Map<Long, List<MentorAnswerReaction>> reactionsByAnswer
    ) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", a.getId());
        map.put("content", a.getContent());
        map.put("isAccepted", a.getIsAccepted());
        map.put("createdAt", a.getCreatedAt());
        map.put("updatedAt", a.getUpdatedAt());
        map.put("isEdited", a.getIsEdited() != null ? a.getIsEdited() : false);
        map.put("depth", a.getDepth() != null ? a.getDepth() : 0);
        map.put("parentAnswerId", a.getParentAnswer() != null ? a.getParentAnswer().getId() : null);

        // Author details
        map.put("authorId", a.getAuthor().getId());
        map.put("authorName", a.getAuthor().getName());
        map.put("authorAvatar", a.getAuthor().getName() != null && !a.getAuthor().getName().isEmpty()
                ? a.getAuthor().getName().substring(0, 1).toUpperCase() : "U");
        map.put("authorProfileImageUrl", a.getAuthor().getProfileImageUrl());
        map.put("authorReputation", a.getAuthor().getReputationPoints() != null ? a.getAuthor().getReputationPoints() : 0);
        map.put("authorLevel", a.getAuthor().getReputationLevel() != null ? a.getAuthor().getReputationLevel() : "BEGINNER");

        // Votes
        List<MentorAnswerVote> votes = votesByAnswer.getOrDefault(a.getId(), Collections.emptyList());
        long upvotes = votes.stream().filter(v -> "UP".equals(v.getVoteType())).count();
        long downvotes = votes.stream().filter(v -> "DOWN".equals(v.getVoteType())).count();
        map.put("votesScore", upvotes - downvotes);

        String userVote = null;
        if (currentUserId != null) {
            userVote = votes.stream()
                    .filter(v -> v.getUser().getId().equals(currentUserId))
                    .map(MentorAnswerVote::getVoteType)
                    .findFirst().orElse(null);
        }
        map.put("userVote", userVote);

        // Reactions
        List<MentorAnswerReaction> reactions = reactionsByAnswer.getOrDefault(a.getId(), Collections.emptyList());
        Map<String, Long> reactionCounts = reactions.stream()
                .collect(Collectors.groupingBy(MentorAnswerReaction::getEmoji, Collectors.counting()));
        map.put("reactionCounts", reactionCounts);

        List<String> userReactions = Collections.emptyList();
        if (currentUserId != null) {
            userReactions = reactions.stream()
                    .filter(r -> r.getUser().getId().equals(currentUserId))
                    .map(MentorAnswerReaction::getEmoji)
                    .collect(Collectors.toList());
        }
        map.put("userReactions", userReactions);

        // Recursive child replies
        List<MentorAnswer> childReplies = allAnswers.stream()
                .filter(reply -> reply.getParentAnswer() != null && reply.getParentAnswer().getId().equals(a.getId()))
                .collect(Collectors.toList());

        // Sort child replies by creation date (chronological)
        childReplies.sort(Comparator.comparing(MentorAnswer::getCreatedAt));

        List<Map<String, Object>> mappedReplies = childReplies.stream()
                .map(reply -> mapAnswerToHierarchy(reply, allAnswers, currentUserId, votesByAnswer, reactionsByAnswer))
                .collect(Collectors.toList());
        map.put("replies", mappedReplies);

        return map;
    }

    private void parseAndNotifyMentions(String content, MentorAnswer answer) {
        if (content == null || content.isEmpty()) {
            return;
        }

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("@([a-zA-Z0-9_\\.]+)");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        Set<String> mentionedNames = new HashSet<>();
        while (matcher.find()) {
            mentionedNames.add(matcher.group(1));
        }

        if (mentionedNames.isEmpty()) {
            return;
        }

        List<User> activeUsers = userRepo.findAll();
        Set<Long> notifiedUserIds = new HashSet<>();

        for (String mention : mentionedNames) {
            String sanitizedMention = mention.toLowerCase().replace("_", "").replace(".", "");
            for (User u : activeUsers) {
                if (u.getActive() != null && u.getActive() && !u.getId().equals(answer.getAuthor().getId())) {
                    String sanitizedName = u.getName().toLowerCase().replace(" ", "").replace("_", "").replace(".", "");
                    if (sanitizedName.equals(sanitizedMention) || sanitizedName.startsWith(sanitizedMention)) {
                        if (!notifiedUserIds.contains(u.getId())) {
                            notifiedUserIds.add(u.getId());
                            notificationService.notifyUserInAppOnly(
                                u,
                                "You were mentioned in a discussion",
                                answer.getAuthor().getName() + " mentioned you in a comment on '" + answer.getQuestion().getTitle() + "'",
                                "QA_MENTION",
                                "/qa/" + answer.getQuestion().getId()
                            );
                        }
                    }
                }
            }
        }
        
        if (!notifiedUserIds.isEmpty()) {
            String idsStr = notifiedUserIds.stream().map(Object::toString).collect(Collectors.joining(","));
            answer.setMentionedUserIds(idsStr);
            answerRepo.save(answer);
        }
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<User> page = userRepo.findByNameContainingIgnoreCase(query.trim(), pageRequest);
        return page.getContent().stream()
                .filter(u -> Boolean.TRUE.equals(u.getActive()))
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("name", u.getName());
                    m.put("email", u.getEmail());
                    m.put("profileImageUrl", u.getProfileImageUrl());
                    return m;
                }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> votePoll(Long userId, Long pollOptionId) {
        MentorPoll targetOption = pollRepo.findById(pollOptionId)
                .orElseThrow(() -> new RuntimeException("Poll option not found"));
        
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Long questionId = targetOption.getQuestion().getId();
        
        // Find if user already voted on any option for this question
        List<MentorPollVote> existingVotes = pollVoteRepo.findAllByPollQuestionIdAndUserId(questionId, userId);
        
        boolean voted;
        if (!existingVotes.isEmpty()) {
            MentorPollVote oldVote = existingVotes.get(0);
            MentorPoll oldOption = oldVote.getPoll();
            
            if (oldOption.getId().equals(pollOptionId)) {
                // Clicked same option -> retract
                pollVoteRepo.delete(oldVote);
                oldOption.setVotesCount(Math.max(0, oldOption.getVotesCount() - 1));
                pollRepo.save(oldOption);
                voted = false;
            } else {
                // Clicked different option -> switch
                pollVoteRepo.delete(oldVote);
                oldOption.setVotesCount(Math.max(0, oldOption.getVotesCount() - 1));
                pollRepo.save(oldOption);
                
                MentorPollVote newVote = new MentorPollVote();
                newVote.setPoll(targetOption);
                newVote.setUser(user);
                pollVoteRepo.save(newVote);
                
                targetOption.setVotesCount(targetOption.getVotesCount() + 1);
                pollRepo.save(targetOption);
                voted = true;
            }
        } else {
            // New vote
            MentorPollVote newVote = new MentorPollVote();
            newVote.setPoll(targetOption);
            newVote.setUser(user);
            pollVoteRepo.save(newVote);
            
            targetOption.setVotesCount(targetOption.getVotesCount() + 1);
            pollRepo.save(targetOption);
            voted = true;
        }
        
        // Return updated poll data
        List<MentorPoll> polls = pollRepo.findAllByQuestionId(questionId);
        List<Map<String, Object>> optionsList = new ArrayList<>();
        long totalVotes = 0;
        Long votedOptionId = null;
        
        for (MentorPoll p : polls) {
            Map<String, Object> pm = new HashMap<>();
            pm.put("id", p.getId());
            pm.put("optionText", p.getOptionText());
            pm.put("votesCount", p.getVotesCount() != null ? p.getVotesCount() : 0);
            totalVotes += (p.getVotesCount() != null ? p.getVotesCount() : 0);
            optionsList.add(pm);
        }
        
        if (voted) {
            votedOptionId = pollOptionId;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("options", optionsList);
        result.put("totalVotes", totalVotes);
        result.put("hasVoted", voted);
        result.put("votedOptionId", votedOptionId);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLeaderboard() {
        List<User> topUsers = userRepo.findTop10ByDeletedFalseAndActiveTrueOrderByReputationPointsDesc();
        return topUsers.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getName());
            map.put("profileImageUrl", u.getProfileImageUrl());
            map.put("reputationPoints", u.getReputationPoints() != null ? u.getReputationPoints() : 0);
            map.put("reputationLevel", u.getReputationLevel() != null ? u.getReputationLevel() : "BEGINNER");
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserProfileDetails(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("profileImageUrl", user.getProfileImageUrl());
        result.put("reputationPoints", user.getReputationPoints() != null ? user.getReputationPoints() : 0);
        result.put("reputationLevel", user.getReputationLevel() != null ? user.getReputationLevel() : "BEGINNER");
        result.put("bio", user.getBio());
        result.put("skills", user.getSkills());
        result.put("socialLinks", user.getSocialLinks());
        result.put("memberSince", user.getMemberSince() != null ? user.getMemberSince() : user.getCreatedAt());

        // Reputation history breakdown
        Map<String, Object> repDetails = reputationService.getUserReputationDetails(userId);
        result.put("reputationHistory", repDetails.get("logs"));

        // User's recent questions
        List<MentorQuestion> questions = questionRepo.findAllByAuthorIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> mappedQuestions = questions.stream().limit(10).map(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("title", q.getTitle());
            map.put("isSolved", q.getIsSolved());
            map.put("createdAt", q.getCreatedAt());
            map.put("answersCount", q.getAnswers().size());
            return map;
        }).collect(Collectors.toList());
        result.put("recentQuestions", mappedQuestions);

        // User's recent answers
        List<MentorAnswer> answers = answerRepo.findAllByAuthorIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> mappedAnswers = answers.stream().limit(10).map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("questionId", a.getQuestion().getId());
            map.put("questionTitle", a.getQuestion().getTitle());
            map.put("content", a.getContent());
            map.put("isAccepted", a.getIsAccepted());
            map.put("createdAt", a.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
        result.put("recentAnswers", mappedAnswers);

        return result;
    }

    @Transactional
    public Map<String, Object> updateProfile(Long userId, String bio, String skills, String socialLinks) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBio(bio != null ? bio.trim() : null);
        user.setSkills(skills != null ? skills.trim() : null);
        user.setSocialLinks(socialLinks != null ? socialLinks.trim() : null);
        userRepo.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Profile updated successfully");
        return result;
    }
}
