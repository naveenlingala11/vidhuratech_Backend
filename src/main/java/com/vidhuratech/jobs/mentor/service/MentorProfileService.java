package com.vidhuratech.jobs.mentor.service;

import com.vidhuratech.jobs.mentor.dto.MentorDashboardResponse;
import com.vidhuratech.jobs.mentor.dto.MentorProfileRequest;
import com.vidhuratech.jobs.mentor.dto.MentorProfileResponse;
import com.vidhuratech.jobs.mentor.entity.MentorProfile;
import com.vidhuratech.jobs.mentor.repository.MentorProfileRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import com.vidhuratech.jobs.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vidhuratech.jobs.mentor.dto.MentorApplicationRequest;
import com.vidhuratech.jobs.mentor.entity.MentorStudentRelation;
import com.vidhuratech.jobs.mentor.entity.MentorSession;
import com.vidhuratech.jobs.mentor.entity.MentorGoal;
import com.vidhuratech.jobs.mentor.repository.MentorStudentRelationRepository;
import com.vidhuratech.jobs.mentor.repository.MentorSessionRepository;
import com.vidhuratech.jobs.mentor.repository.MentorGoalRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorProfileService {

    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MentorStudentRelationRepository mentorStudentRelationRepository;
    private final MentorSessionRepository mentorSessionRepository;
    private final MentorGoalRepository mentorGoalRepository;
    private final ActivityNotificationService notificationService;

    @Cacheable(value = "public_mentors", key = "#keyword != null ? #keyword : ''")
    @Transactional(readOnly = true)
    public List<MentorProfileResponse> getActiveMentors(String keyword) {
        List<MentorProfile> profiles;
        if (keyword != null && !keyword.trim().isEmpty()) {
            profiles = mentorProfileRepository.searchActiveMentors(keyword.trim());
        } else {
            profiles = mentorProfileRepository.findAllActiveWithUser();
        }
        return profiles.stream()
                .map(MentorProfileResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MentorProfileResponse getMentorProfileById(Long userId) {
        MentorProfile profile = mentorProfileRepository.findByIdWithUser(userId)
                .orElseThrow(() -> new RuntimeException("Mentor profile not found for ID: " + userId));
        if (!profile.getActive()) {
            throw new RuntimeException("Mentor profile is not active");
        }
        return new MentorProfileResponse(profile);
    }

    @Transactional
    public MentorProfileResponse getOrCreateProfile(Long userId) {
        MentorProfile profile = mentorProfileRepository.findByIdWithUser(userId).orElse(null);
        if (profile == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));
            profile = new MentorProfile();
            profile.setUser(user);
            profile.setUserId(userId);
            profile.setActive(false);
            profile.setFeatured(false);
            profile = mentorProfileRepository.save(profile);
        }
        return new MentorProfileResponse(profile);
    }

    @CacheEvict(value = "public_mentors", allEntries = true)
    @Transactional
    public MentorProfileResponse updateProfile(Long userId, MentorProfileRequest req) {
        MentorProfile profile = mentorProfileRepository.findById(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));
                    MentorProfile mp = new MentorProfile();
                    mp.setUser(user);
                    mp.setUserId(userId);
                    return mp;
                });

        profile.setCurrentCompany(req.getCurrentCompany());
        profile.setCurrentRole(req.getCurrentRole());
        profile.setYearsOfExperience(req.getYearsOfExperience());
        profile.setBiography(req.getBiography());
        profile.setSkills(req.getSkills());
        profile.setLanguages(req.getLanguages());
        profile.setLinkedinUrl(req.getLinkedinUrl());
        profile.setGithubUrl(req.getGithubUrl());
        profile.setPricePerHour(req.getPricePerHour());
        profile.setPricePerWeek(req.getPricePerWeek() != null ? req.getPricePerWeek() : BigDecimal.ZERO);
        profile.setPricePerMonth(req.getPricePerMonth() != null ? req.getPricePerMonth() : BigDecimal.ZERO);
        profile.setAvailabilityDays(req.getAvailabilityDays() != null ? req.getAvailabilityDays()
                : "monday,tuesday,wednesday,thursday,friday");
        profile.setAvailabilitySlots(req.getAvailabilitySlots() != null ? req.getAvailabilitySlots() : "evening");
        profile.setAllowDailySessions(req.getAllowDailySessions() != null ? req.getAllowDailySessions() : false);

        MentorProfile saved = mentorProfileRepository.save(profile);
        return new MentorProfileResponse(saved);
    }

    @CacheEvict(value = "public_mentors", allEntries = true)
    @Transactional
    public void toggleActiveStatus(Long userId, boolean active) {
        MentorProfile profile = mentorProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Mentor profile not found"));

        if (active) {
            int passCount = 0;
            if (Boolean.TRUE.equals(profile.getIdentityVerified()))
                passCount++;
            if (Boolean.TRUE.equals(profile.getCompanyVerified()))
                passCount++;
            if (Boolean.TRUE.equals(profile.getLinkedinVerified()))
                passCount++;
            if (Boolean.TRUE.equals(profile.getCertVerified()))
                passCount++;
            if (Boolean.TRUE.equals(profile.getTermsVerified()))
                passCount++;

            double percentage = (passCount / 5.0) * 100.0;
            if (percentage < 60.0) {
                throw new RuntimeException("Cannot publish mentor: Checklist verification score is " + (int) percentage
                        + "%. Must be at least 60% (3/5 items checked) to publish.");
            }
        }

        profile.setActive(active);
        mentorProfileRepository.save(profile);
    }

    @CacheEvict(value = "public_mentors", allEntries = true)
    @Transactional
    public void toggleFeaturedStatus(Long userId, boolean featured) {
        MentorProfile profile = mentorProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Mentor profile not found"));
        profile.setFeatured(featured);
        mentorProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public List<MentorProfileResponse> getAllMentors() {
        return mentorProfileRepository.findAllWithUser().stream()
                .map(MentorProfileResponse::new)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "public_mentors", allEntries = true)
    @Transactional
    public MentorProfileResponse promoteToMentor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));

        user.setRole(UserRole.MENTOR);
        userRepository.save(user);

        MentorProfile profile = mentorProfileRepository.findById(userId).orElse(null);
        if (profile == null) {
            profile = new MentorProfile();
            profile.setUser(user);
            profile.setUserId(userId);
            profile.setActive(false);
            profile.setFeatured(false);
            profile = mentorProfileRepository.save(profile);
        }
        return new MentorProfileResponse(profile);
    }

    @CacheEvict(value = "public_mentors", allEntries = true)
    @Transactional
    public void demoteFromMentor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));

        user.setRole(UserRole.STUDENT);
        userRepository.save(user);

        if (mentorProfileRepository.existsById(userId)) {
            mentorProfileRepository.deleteById(userId);
        }
    }

    @CacheEvict(value = "public_mentors", allEntries = true)
    @Transactional
    public MentorProfileResponse updateVerification(
            Long userId,
            Boolean identityVerified,
            Boolean companyVerified,
            Boolean linkedinVerified,
            Boolean certVerified,
            Boolean termsVerified,
            String verificationDocumentUrl) {
        MentorProfile profile = mentorProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Mentor profile not found"));

        if (identityVerified != null)
            profile.setIdentityVerified(identityVerified);
        if (companyVerified != null)
            profile.setCompanyVerified(companyVerified);
        if (linkedinVerified != null)
            profile.setLinkedinVerified(linkedinVerified);
        if (certVerified != null)
            profile.setCertVerified(certVerified);
        if (termsVerified != null)
            profile.setTermsVerified(termsVerified);
        if (verificationDocumentUrl != null)
            profile.setVerificationDocumentUrl(verificationDocumentUrl);

        // Auto de-publish if score drops below 60%
        int passCount = 0;
        if (Boolean.TRUE.equals(profile.getIdentityVerified()))
            passCount++;
        if (Boolean.TRUE.equals(profile.getCompanyVerified()))
            passCount++;
        if (Boolean.TRUE.equals(profile.getLinkedinVerified()))
            passCount++;
        if (Boolean.TRUE.equals(profile.getCertVerified()))
            passCount++;
        if (Boolean.TRUE.equals(profile.getTermsVerified()))
            passCount++;

        double percentage = (passCount / 5.0) * 100.0;
        if (percentage < 60.0 && Boolean.TRUE.equals(profile.getActive())) {
            profile.setActive(false);
        }

        MentorProfile saved = mentorProfileRepository.save(profile);
        return new MentorProfileResponse(saved);
    }

    @CacheEvict(value = "public_mentors", allEntries = true)
    @Transactional
    public MentorProfileResponse applyAsMentor(MentorApplicationRequest req) {
        String email = req.getEmail() == null ? "" : req.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("EMAIL_ALREADY_EXISTS");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(email);
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(UserRole.MENTOR);
        user.setActive(true);
        user.setFirstLogin(false);
        user.setCreatedAt(LocalDateTime.now());
        if (req.getProfileImageUrl() != null && !req.getProfileImageUrl().isBlank()) {
            user.setProfileImageUrl(req.getProfileImageUrl());
        }

        User savedUser = userRepository.saveAndFlush(user);

        MentorProfile profile = new MentorProfile();
        profile.setUser(savedUser);
        profile.setCurrentCompany(req.getCurrentCompany());
        profile.setCurrentRole(req.getCurrentRole());
        profile.setYearsOfExperience(req.getYearsOfExperience());
        profile.setBiography(req.getBiography());
        profile.setSkills(req.getSkills());
        profile.setLanguages(req.getLanguages());
        profile.setLinkedinUrl(req.getLinkedinUrl());
        profile.setGithubUrl(req.getGithubUrl());
        profile.setPricePerHour(req.getPricePerHour());
        profile.setVerificationDocumentUrl(req.getVerificationDocumentUrl());

        profile.setActive(false); // unverified by default
        profile.setFeatured(false);
        profile.setIdentityVerified(false);
        profile.setCompanyVerified(false);
        profile.setLinkedinVerified(false);
        profile.setCertVerified(false);
        profile.setTermsVerified(false);

        MentorProfile savedProfile = mentorProfileRepository.saveAndFlush(profile);
        return new MentorProfileResponse(savedProfile);
    }

    @Transactional(readOnly = true)
    public MentorDashboardResponse getDashboardData(Long userId) {
        MentorProfile profile = mentorProfileRepository.findByIdWithUser(userId)
                .orElseThrow(() -> new RuntimeException("Mentor profile not found for ID: " + userId));

        MentorDashboardResponse dashboard = new MentorDashboardResponse();
        dashboard.setProfile(new MentorProfileResponse(profile));

        // Pull active relationships
        List<MentorStudentRelation> relations = mentorStudentRelationRepository.findAllByMentorIdWithStudent(userId);

        // Pull sessions
        List<MentorSession> sessions = mentorSessionRepository.findAllByMentorIdWithStudent(userId);

        // Pull goals
        List<MentorGoal> goals = mentorGoalRepository.findAllByMentorIdOrderByCompletedAscIdDesc(userId);

        // Calculate Stats
        dashboard.setMenteesCount(relations.size());

        long upcomingCount = sessions.stream().filter(s -> "SCHEDULED".equals(s.getStatus())).count();
        dashboard.setUpcomingSessionsCount((int) upcomingCount);

        long completedCount = sessions.stream().filter(s -> "COMPLETED".equals(s.getStatus())).count();
        dashboard.setCompletedSessionsCount((int) completedCount);

        int avgProgress = 0;
        if (!relations.isEmpty()) {
            int totalProgress = 0;
            for (MentorStudentRelation r : relations) {
                totalProgress += r.getProgress();
            }
            avgProgress = totalProgress / relations.size();
        }
        dashboard.setAvgProgress(avgProgress);

        long pendingFeedbackCount = relations.stream().filter(r -> r.getProgress() < 100).count();
        dashboard.setPendingFeedbackCount((int) pendingFeedbackCount);

        // Dynamic Earnings Calculator (Hourly sessions completed + active
        // subscriptions)
        BigDecimal rate = profile.getPricePerHour() != null ? profile.getPricePerHour() : BigDecimal.ZERO;
        BigDecimal hourlyEarnings = rate.multiply(BigDecimal.valueOf(completedCount));

        BigDecimal weeklyRate = profile.getPricePerWeek() != null ? profile.getPricePerWeek() : BigDecimal.ZERO;
        long activeCount = relations.stream().filter(r -> "ACTIVE".equals(r.getStatus())).count();
        BigDecimal weeklyEarnings = weeklyRate.multiply(BigDecimal.valueOf(activeCount));

        dashboard.setTotalEarnings(hourlyEarnings.add(weeklyEarnings));

        // Map MenteeProgressItems
        List<MentorDashboardResponse.MenteeProgressItem> menteeProgressList = relations.stream().map(r -> {
            MentorDashboardResponse.MenteeProgressItem item = new MentorDashboardResponse.MenteeProgressItem();
            item.setRelationId(r.getId());
            item.setName(r.getStudent().getName());
            item.setProgress(r.getProgress());
            item.setAvatar(r.getStudent().getName().substring(0, 1).toUpperCase());
            item.setLastMeetingDate(r.getLastMeetingDate());
            item.setTargetMilestone(r.getTargetMilestone());
            return item;
        }).collect(Collectors.toList());
        dashboard.setMenteeProgressList(menteeProgressList);

        // Map UpcomingMeetingItems
        List<MentorDashboardResponse.UpcomingMeetingItem> upcomingMeetingsList = sessions.stream().map(s -> {
            MentorDashboardResponse.UpcomingMeetingItem item = new MentorDashboardResponse.UpcomingMeetingItem();
            item.setMentee(s.getStudent().getName());
            item.setDate(s.getSessionDate());
            item.setTime(s.getSessionTime());
            item.setType(s.getSessionType());
            item.setMeetingLink(s.getMeetingLink());
            return item;
        }).collect(Collectors.toList());
        dashboard.setUpcomingMeetingsList(upcomingMeetingsList);

        // Map GoalItems
        List<MentorDashboardResponse.GoalItem> goalsList = goals.stream().map(g -> {
            MentorDashboardResponse.GoalItem item = new MentorDashboardResponse.GoalItem();
            item.setTitle(g.getTitle());
            item.setDescription(g.getDescription());
            item.setCompleted(g.getCompleted());
            item.setDueDate(g.getDueDate());
            return item;
        }).collect(Collectors.toList());
        dashboard.setGoalsList(goalsList);

        return dashboard;
    }

    @Transactional
    public void saveAvailability(Long userId, String days, String slots, Boolean allowDaily) {
        MentorProfile profile = mentorProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Mentor profile not found"));
        profile.setAvailabilityDays(days);
        profile.setAvailabilitySlots(slots);
        profile.setAllowDailySessions(allowDaily);
        mentorProfileRepository.save(profile);
    }

    @Transactional
    public void scheduleSession(Long userId, String studentName, String date, String time, String type, String link) {
        User mentor = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Mentor user not found"));

        // Find the student relation to verify membership
        MentorStudentRelation relation = mentorStudentRelationRepository
                .findByMentorIdAndStudentName(userId, studentName)
                .orElseThrow(() -> new RuntimeException("Student not assigned in your roster: " + studentName));

        MentorSession session = new MentorSession();
        session.setMentor(mentor);
        session.setStudent(relation.getStudent());
        session.setSessionDate(date);
        session.setSessionTime(time);
        session.setSessionType(type);
        session.setMeetingLink(link);
        session.setStatus("SCHEDULED");

        mentorSessionRepository.save(session);
    }

    @Transactional
    public void submitFeedback(Long userId, String studentName, Integer progress, String milestone, String note) {
        User mentor = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Mentor user not found"));

        MentorStudentRelation relation = mentorStudentRelationRepository
                .findByMentorIdAndStudentName(userId, studentName)
                .orElseThrow(() -> new RuntimeException("Student not assigned in your roster: " + studentName));

        // Update student progress & milestone
        relation.setProgress(progress);
        relation.setLastMeetingDate("Just now");
        if (milestone != null && !milestone.trim().isEmpty()) {
            relation.setTargetMilestone(milestone.trim());
        }
        mentorStudentRelationRepository.save(relation);

        // Add a follow-up Goal/Task
        MentorGoal goal = new MentorGoal();
        goal.setMentor(mentor);
        goal.setTitle(studentName + ": Feedback Follow-up");
        goal.setDescription(note);
        goal.setCompleted(false);
        goal.setDueDate("Next Session");

        mentorGoalRepository.save(goal);
    }

    @Transactional
    public void sendSessionInvite(Long userId, Long sessionId) {
        MentorSession session = mentorSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getMentor().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You are not the assigned mentor for this session");
        }

        User student = session.getStudent();
        String title = "Mock Interview Room Invite";
        String message = String.format(
                "Hi %s, your mentor %s has invited you to join the mock interview room. Date: %s at %s.",
                student.getName(), session.getMentor().getName(), session.getSessionDate(), session.getSessionTime());
        String link = "/dashboard/meeting/VidhuraTech_Meeting_Session_" + sessionId;

        notificationService.notifyUser(student, title, message, "MENTOR_SESSION", link);
    }
}
