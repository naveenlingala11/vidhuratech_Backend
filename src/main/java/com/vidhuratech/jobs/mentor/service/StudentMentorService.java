package com.vidhuratech.jobs.mentor.service;

import com.vidhuratech.jobs.mentor.entity.MentorProfile;
import com.vidhuratech.jobs.mentor.entity.MentorSession;
import com.vidhuratech.jobs.mentor.entity.MentorStudentRelation;
import com.vidhuratech.jobs.mentor.repository.MentorProfileRepository;
import com.vidhuratech.jobs.mentor.repository.MentorSessionRepository;
import com.vidhuratech.jobs.mentor.repository.MentorStudentRelationRepository;
import com.vidhuratech.jobs.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentMentorService {

    private final MentorStudentRelationRepository relationRepository;
    private final MentorSessionRepository sessionRepository;
    private final MentorProfileRepository mentorProfileRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentMentorDashboard(Long studentId) {
        Map<String, Object> result = new HashMap<>();

        // Get assigned mentor relations
        List<MentorStudentRelation> relations = relationRepository.findAllByStudentIdWithMentor(studentId);

        // Get sessions
        List<MentorSession> sessions = sessionRepository.findAllByStudentIdWithMentor(studentId);

        // Stats
        result.put("totalMentors", relations.size());
        result.put("activeMentors", relations.stream().filter(r -> "ACTIVE".equals(r.getStatus())).count());

        long upcomingSessions = sessions.stream().filter(s -> "SCHEDULED".equals(s.getStatus())).count();
        long completedSessions = sessions.stream().filter(s -> "COMPLETED".equals(s.getStatus())).count();
        result.put("upcomingSessions", upcomingSessions);
        result.put("completedSessions", completedSessions);
        result.put("totalSessions", sessions.size());

        // Average progress across all mentor relations
        int avgProgress = 0;
        if (!relations.isEmpty()) {
            int totalProgress = 0;
            for (MentorStudentRelation r : relations) {
                totalProgress += r.getProgress();
            }
            avgProgress = totalProgress / relations.size();
        }
        result.put("avgProgress", avgProgress);

        // Map mentor list
        List<Map<String, Object>> mentorList = relations.stream().map(r -> {
            Map<String, Object> mentorMap = new HashMap<>();
            User mentor = r.getMentor();
            mentorMap.put("relationId", r.getId());
            mentorMap.put("mentorId", mentor.getId());
            mentorMap.put("mentorName", mentor.getName());
            mentorMap.put("mentorEmail", "hidden_for_security");
            mentorMap.put("mentorAvatar", mentor.getName().substring(0, 1).toUpperCase());
            mentorMap.put("progress", r.getProgress());
            mentorMap.put("targetMilestone", r.getTargetMilestone());
            mentorMap.put("lastMeetingDate", r.getLastMeetingDate());
            mentorMap.put("status", r.getStatus());
            mentorMap.put("createdAt", r.getCreatedAt());

            // Try to get mentor profile details
            Optional<MentorProfile> profileOpt = mentorProfileRepository.findByIdWithUser(mentor.getId());
            if (profileOpt.isPresent()) {
                MentorProfile profile = profileOpt.get();
                mentorMap.put("currentCompany", profile.getCurrentCompany());
                mentorMap.put("currentRole", profile.getCurrentRole());
                mentorMap.put("yearsOfExperience", profile.getYearsOfExperience());
                mentorMap.put("skills", profile.getSkills());
                mentorMap.put("pricePerHour", profile.getPricePerHour());
                mentorMap.put("pricePerWeek", profile.getPricePerWeek());
                mentorMap.put("pricePerMonth", profile.getPricePerMonth());
                mentorMap.put("rating", profile.getRating());
                mentorMap.put("reviewsCount", profile.getReviewsCount());
                mentorMap.put("profileImageUrl", mentor.getProfileImageUrl());
                mentorMap.put("availabilityDays", profile.getAvailabilityDays());
                mentorMap.put("availabilitySlots", profile.getAvailabilitySlots());
            }

            return mentorMap;
        }).collect(Collectors.toList());
        result.put("mentors", mentorList);

        // Map sessions list
        List<Map<String, Object>> sessionList = sessions.stream().map(s -> {
            Map<String, Object> sessionMap = new HashMap<>();
            sessionMap.put("id", s.getId());
            sessionMap.put("mentorId", s.getMentor().getId());
            sessionMap.put("mentorName", s.getMentor().getName());
            sessionMap.put("mentorAvatar", s.getMentor().getName().substring(0, 1).toUpperCase());
            sessionMap.put("date", s.getSessionDate());
            sessionMap.put("time", s.getSessionTime());
            sessionMap.put("type", s.getSessionType());
            sessionMap.put("meetingLink", s.getMeetingLink());
            sessionMap.put("status", s.getStatus());
            sessionMap.put("createdAt", s.getCreatedAt());
            return sessionMap;
        }).collect(Collectors.toList());
        result.put("sessions", sessionList);

        return result;
    }
}
