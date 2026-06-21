package com.vidhuratech.jobs.mentor.service;

import com.vidhuratech.jobs.mentor.entity.MentorProfile;
import com.vidhuratech.jobs.mentor.entity.MentorReview;
import com.vidhuratech.jobs.mentor.repository.MentorProfileRepository;
import com.vidhuratech.jobs.mentor.repository.MentorReviewRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorReviewService {

    private final MentorReviewRepository reviewRepository;
    private final MentorProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getReviewsForMentor(Long mentorId) {
        Map<String, Object> result = new HashMap<>();

        List<MentorReview> reviews = reviewRepository.findPublishedByMentorId(mentorId);
        Double avgRating = reviewRepository.getAverageRatingByMentorId(mentorId);
        long totalCount = reviewRepository.countByMentorIdAndStatus(mentorId, "PUBLISHED");

        result.put("averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 5.0);
        result.put("totalReviews", totalCount);

        // Rating breakdown (count per star)
        Map<Integer, Long> breakdown = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int star = i;
            breakdown.put(i, reviews.stream().filter(r -> r.getRating() == star).count());
        }
        result.put("ratingBreakdown", breakdown);

        List<Map<String, Object>> reviewList = reviews.stream().map(r -> {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("id", r.getId());
            reviewMap.put("rating", r.getRating());
            reviewMap.put("reviewText", r.getReviewText());
            reviewMap.put("sessionType", r.getSessionType());
            reviewMap.put("createdAt", r.getCreatedAt());
            reviewMap.put("studentName", r.getStudent().getName());
            reviewMap.put("studentAvatar", r.getStudent().getName().substring(0, 1).toUpperCase());
            return reviewMap;
        }).collect(Collectors.toList());

        result.put("reviews", reviewList);
        return result;
    }

    @CacheEvict(value = "public_mentors", allEntries = true)
    @Transactional
    public Map<String, Object> submitReview(Long studentId, Long mentorId, Integer rating, String reviewText, String sessionType) {
        // Check if already reviewed
        if (reviewRepository.existsByMentorIdAndStudentId(mentorId, studentId)) {
            throw new RuntimeException("You have already reviewed this mentor");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        MentorReview review = new MentorReview();
        review.setStudent(student);
        review.setMentor(mentor);
        review.setRating(rating);
        review.setReviewText(reviewText);
        review.setSessionType(sessionType);
        review.setStatus("PUBLISHED");

        reviewRepository.save(review);

        // Update mentor profile aggregate rating
        Double newAvg = reviewRepository.getAverageRatingByMentorId(mentorId);
        long newCount = reviewRepository.countByMentorIdAndStatus(mentorId, "PUBLISHED");

        MentorProfile profile = profileRepository.findById(mentorId).orElse(null);
        if (profile != null) {
            profile.setRating(newAvg != null ? Math.round(newAvg * 10.0) / 10.0 : 5.0);
            profile.setReviewsCount((int) newCount);
            profileRepository.save(profile);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Review submitted successfully");
        result.put("newRating", newAvg != null ? Math.round(newAvg * 10.0) / 10.0 : 5.0);
        result.put("totalReviews", newCount);
        return result;
    }
}
