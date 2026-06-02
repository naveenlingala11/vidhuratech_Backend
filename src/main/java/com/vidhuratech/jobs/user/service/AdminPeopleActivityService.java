package com.vidhuratech.jobs.user.service;

import com.vidhuratech.jobs.leads.entity.Lead;
import com.vidhuratech.jobs.leads.repository.LeadRepository;
import com.vidhuratech.jobs.lms.batch.entity.BatchEnrollment;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.plans.entity.PlanAccessGrant;
import com.vidhuratech.jobs.plans.repository.PlanAccessGrantRepository;
import com.vidhuratech.jobs.publicpractice.entity.PublicAssessmentAttempt;
import com.vidhuratech.jobs.publicpractice.entity.PublicChallengeAttempt;
import com.vidhuratech.jobs.publicpractice.repository.PublicAssessmentAttemptRepository;
import com.vidhuratech.jobs.publicpractice.repository.PublicChallengeAttemptRepository;
import com.vidhuratech.jobs.trainer.entity.AssessmentAttempt;
import com.vidhuratech.jobs.trainer.entity.MockInterviewRequest;
import com.vidhuratech.jobs.trainer.entity.PseudoCodeAttempt;
import com.vidhuratech.jobs.trainer.repository.AssessmentAttemptRepository;
import com.vidhuratech.jobs.trainer.repository.MockInterviewRequestRepository;
import com.vidhuratech.jobs.trainer.repository.PseudoCodeAttemptRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPeopleActivityService {

    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final PlanAccessGrantRepository planAccessGrantRepository;
    private final PublicAssessmentAttemptRepository publicAssessmentAttemptRepository;
    private final PublicChallengeAttemptRepository publicChallengeAttemptRepository;
    private final AssessmentAttemptRepository assessmentAttemptRepository;
    private final PseudoCodeAttemptRepository pseudoCodeAttemptRepository;
    private final MockInterviewRequestRepository mockInterviewRequestRepository;
    private final BatchEnrollmentRepository batchEnrollmentRepository;

    public Map<String, Object> people360(String keyword) {
        String term = safe(keyword).toLowerCase();
        Map<String, Person> people = collectPeople();

        people.values().forEach(this::fillCounts);

        List<Map<String, Object>> rows = people.values()
                .stream()
                .filter(p -> term.isBlank() || String.valueOf(p.toMap()).toLowerCase().contains(term))
                .sorted((a, b) -> safeDate(b.lastActivityAt).compareTo(safeDate(a.lastActivityAt)))
                .map(Person::toMap)
                .toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", rows);
        data.put("totalElements", rows.size());
        data.put("totalUsers", people.values().stream().filter(p -> p.userId != null).count());
        data.put("totalLeads", people.values().stream().filter(p -> !p.leadIds.isEmpty()).count());
        return data;
    }

    public Map<String, Object> history(String key) {
        Person person = collectPeople().get(key);

        if (person == null) {
            throw new RuntimeException("Person not found");
        }

        fillCounts(person);

        List<Map<String, Object>> timeline = new ArrayList<>();

        for (PlanAccessGrant g : planAccessGrantRepository.findAll()) {
            if (samePerson(person, g.getBuyerEmail(), g.getBuyerPhone(), g.getUserId())) {
                timeline.add(item(
                        "PLAN_ACCESS",
                        safe(g.getPlanName()),
                        safe(g.getStatus()),
                        g.getCreatedAt(),
                        meta(
                                "planCode", g.getPlanCode(),
                                "amount", g.getAmount(),
                                "startsAt", g.getStartsAt(),
                                "expiresAt", g.getExpiresAt()
                        )
                ));
            }
        }

        for (BatchEnrollment e : batchEnrollmentRepository.findAll()) {
            if (e.getStudent() != null && samePerson(person, e.getStudent().getEmail(), e.getStudent().getPhone(), e.getStudent().getId())) {
                timeline.add(item(
                        "COURSE_ENROLLMENT",
                        e.getBatch() == null ? "Course enrollment" : "Batch #" + e.getBatch().getId(),
                        Boolean.TRUE.equals(e.getActive()) ? "ACTIVE" : "INACTIVE",
                        e.getEnrolledAt(),
                        meta("batchId", e.getBatch() == null ? null : e.getBatch().getId())
                ));
            }
        }

        for (PublicChallengeAttempt a : publicChallengeAttemptRepository.findAll()) {
            if (samePerson(person, a.getParticipantEmail(), a.getParticipantPhone(), a.getUserId()) || sameLead(person, a.getLeadId())) {
                timeline.add(item(
                        "PUBLIC_CODING_CHALLENGE",
                        "Challenge #" + a.getChallengeId(),
                        safe(a.getStatus()),
                        a.getSubmittedAt(),
                        meta(
                                "score", a.getScore(),
                                "percentage", a.getPercentage(),
                                "language", a.getLanguage()
                        )
                ));
            }
        }

        for (PublicAssessmentAttempt a : publicAssessmentAttemptRepository.findAll()) {
            if (sameLead(person, a.getLeadId())) {
                timeline.add(item(
                        "PUBLIC_MOCK_TEST",
                        "Assessment #" + a.getAssessmentId(),
                        safe(a.getStatus()),
                        a.getSubmittedAt(),
                        meta(
                                "score", a.getScore(),
                                "totalMarks", a.getTotalMarks(),
                                "percentage", a.getPercentage()
                        )
                ));
            }
        }

        for (AssessmentAttempt a : assessmentAttemptRepository.findAll()) {
            if (sameStudent(person, a.getStudent())) {
                timeline.add(item(
                        "ASSESSMENT",
                        a.getAssessment() == null ? "Assessment" : "Assessment #" + a.getAssessment().getId(),
                        "SUBMITTED",
                        a.getSubmittedAt(),
                        meta(
                                "score", a.getScore(),
                                "correctAnswers", a.getCorrectAnswers(),
                                "totalQuestions", a.getTotalQuestions()
                        )
                ));
            }
        }

        for (PseudoCodeAttempt a : pseudoCodeAttemptRepository.findAll()) {
            if (sameStudent(person, a.getStudent())) {
                timeline.add(item(
                        "CODING_CHALLENGE",
                        a.getChallenge() == null ? "Coding challenge" : "Challenge #" + a.getChallenge().getId(),
                        safe(a.getStatus()),
                        a.getSubmittedAt(),
                        meta(
                                "score", a.getScore(),
                                "percentage", a.getPercentage(),
                                "language", a.getLanguage(),
                                "allTestsPassed", a.getAllTestsPassed()
                        )
                ));
            }
        }

        for (MockInterviewRequest r : mockInterviewRequestRepository.findAll()) {
            if (r.getStudent() != null && samePerson(person, r.getStudent().getEmail(), r.getStudent().getPhone(), r.getStudent().getId())) {
                timeline.add(item(
                        "MOCK_INTERVIEW",
                        safe(r.getTopic()),
                        String.valueOf(r.getStatus()),
                        r.getCreatedAt(),
                        meta(
                                "preferredDate", r.getPreferredDate(),
                                "preferredTime", r.getPreferredTime(),
                                "trainer", r.getTrainer() == null ? "" : r.getTrainer().getName(),
                                "meetingLink", r.getMeetingLink()
                        )
                ));
            }
        }

        timeline.sort((a, b) -> safeDate((LocalDateTime) b.get("timestamp")).compareTo(safeDate((LocalDateTime) a.get("timestamp"))));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("person", person.toMap());
        data.put("summary", person.summaryMap());
        data.put("timeline", timeline);
        return data;
    }

    private Map<String, Person> collectPeople() {
        Map<String, Person> people = new LinkedHashMap<>();

        for (User user : userRepository.findAll()) {
            String key = personKey(user.getEmail(), user.getPhone(), "USER:" + user.getId());
            Person p = people.computeIfAbsent(key, k -> new Person(k));
            p.userId = user.getId();
            p.name = first(p.name, user.getName());
            p.email = first(p.email, user.getEmail());
            p.phone = first(p.phone, user.getPhone());
            p.role = user.getRole() == null ? "STUDENT" : user.getRole().name();
            p.active = Boolean.TRUE.equals(user.getActive());
            p.deleted = Boolean.TRUE.equals(user.getDeleted());
            p.sources.add("USER");
            p.createdAt = latest(p.createdAt, user.getCreatedAt());
        }

        for (Lead lead : leadRepository.findAll()) {
            String key = personKey(lead.getEmail(), lead.getPhone(), "LEAD:" + lead.getId());
            Person p = people.computeIfAbsent(key, k -> new Person(k));
            p.leadIds.add(lead.getId());
            p.name = first(p.name, lead.getName());
            p.email = first(p.email, lead.getEmail());
            p.phone = first(p.phone, lead.getPhone());
            p.city = first(p.city, lead.getCity());
            p.course = first(p.course, lead.getCourse());
            p.leadStatus = first(p.leadStatus, lead.getStatus());
            p.sources.add("LEAD");
            p.createdAt = latest(p.createdAt, lead.getCreatedAt());
        }

        return people;
    }

    private void fillCounts(Person p) {
        p.resetCounts();

        for (PlanAccessGrant g : planAccessGrantRepository.findAll()) {
            if (samePerson(p, g.getBuyerEmail(), g.getBuyerPhone(), g.getUserId())) {
                p.planAccessCount++;
                p.lastActivityAt = latest(p.lastActivityAt, g.getCreatedAt());
            }
        }

        for (BatchEnrollment e : batchEnrollmentRepository.findAll()) {
            if (sameStudent(p, e.getStudent())) {
                p.courseEnrollments++;
                p.lastActivityAt = latest(p.lastActivityAt, e.getEnrolledAt());
            }
        }

        for (PublicChallengeAttempt a : publicChallengeAttemptRepository.findAll()) {
            if (samePerson(p, a.getParticipantEmail(), a.getParticipantPhone(), a.getUserId()) || sameLead(p, a.getLeadId())) {
                p.codingChallenges++;
                p.lastActivityAt = latest(p.lastActivityAt, a.getSubmittedAt());
            }
        }

        for (PublicAssessmentAttempt a : publicAssessmentAttemptRepository.findAll()) {
            if (sameLead(p, a.getLeadId())) {
                p.mockTests++;
                p.lastActivityAt = latest(p.lastActivityAt, a.getSubmittedAt());
            }
        }

        for (AssessmentAttempt a : assessmentAttemptRepository.findAll()) {
            if (sameStudent(p, a.getStudent())) {
                p.assessments++;
                p.lastActivityAt = latest(p.lastActivityAt, a.getSubmittedAt());
            }
        }

        for (PseudoCodeAttempt a : pseudoCodeAttemptRepository.findAll()) {
            if (sameStudent(p, a.getStudent())) {
                p.codingChallenges++;
                p.lastActivityAt = latest(p.lastActivityAt, a.getSubmittedAt());
            }
        }

        for (MockInterviewRequest r : mockInterviewRequestRepository.findAll()) {
            if (sameStudent(p, r.getStudent())) {
                p.mockInterviews++;
                p.lastActivityAt = latest(p.lastActivityAt, r.getCreatedAt());
            }
        }
    }

    private boolean samePerson(Person p, String email, String phone, Long userId) {
        if (p == null) return false;

        if (p.userId != null && userId != null && Objects.equals(p.userId, userId)) {
            return true;
        }

        String personEmail = safe(p.email).trim().toLowerCase();
        String otherEmail = safe(email).trim().toLowerCase();

        if (!personEmail.isBlank() && !otherEmail.isBlank() && personEmail.equals(otherEmail)) {
            return true;
        }

        String personPhone = digits(p.phone);
        String otherPhone = digits(phone);

        return !personPhone.isBlank() && !otherPhone.isBlank() && personPhone.equals(otherPhone);
    }

    private boolean sameLead(Person p, Long leadId) {
        return leadId != null && p.leadIds != null && p.leadIds.contains(leadId);
    }

    private boolean sameStudent(Person p, User student) {
        if (student == null) return false;
        return samePerson(p, student.getEmail(), student.getPhone(), student.getId());
    }

    private Map<String, Object> item(String type, String title, String status, LocalDateTime timestamp, Map<String, Object> meta) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", type);
        item.put("title", safe(title));
        item.put("status", safe(status));
        item.put("timestamp", timestamp);
        item.put("meta", meta);
        return item;
    }

    private Map<String, Object> meta(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();

        for (int i = 0; i + 1 < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }

        return map;
    }

    private String personKey(String email, String phone, String fallback) {
        if (!safe(email).isBlank()) return "EMAIL:" + safe(email).toLowerCase();
        if (!digits(phone).isBlank()) return "PHONE:" + digits(phone);
        return fallback;
    }

    private LocalDateTime latest(LocalDateTime a, LocalDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    private LocalDateTime safeDate(LocalDateTime date) {
        return date == null ? LocalDateTime.MIN : date;
    }

    private String first(String a, String b) {
        return safe(a).isBlank() ? safe(b) : safe(a);
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String digits(String value) {
        return safe(value).replaceAll("\\D", "");
    }

    private class Person {
        String key;
        Long userId;
        Set<Long> leadIds = new LinkedHashSet<>();
        String name = "";
        String email = "";
        String phone = "";
        String role = "LEAD";
        String city = "";
        String course = "";
        String leadStatus = "";
        Boolean active = true;
        Boolean deleted = false;
        Set<String> sources = new LinkedHashSet<>();
        LocalDateTime createdAt;
        LocalDateTime lastActivityAt;

        int planAccessCount;
        int mockTests;
        int assessments;
        int codingChallenges;
        int mockInterviews;
        int courseEnrollments;

        Person(String key) {
            this.key = key;
        }

        void resetCounts() {
            planAccessCount = 0;
            mockTests = 0;
            assessments = 0;
            codingChallenges = 0;
            mockInterviews = 0;
            courseEnrollments = 0;
            lastActivityAt = createdAt;
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("key", key);
            map.put("userId", userId);
            map.put("leadIds", leadIds);
            map.put("name", name);
            map.put("email", email);
            map.put("phone", phone);
            map.put("role", role);
            map.put("city", city);
            map.put("course", course);
            map.put("leadStatus", leadStatus);
            map.put("active", active);
            map.put("deleted", deleted);
            map.put("sources", sources);
            map.put("createdAt", createdAt);
            map.put("lastActivityAt", lastActivityAt);
            map.put("planAccessCount", planAccessCount);
            map.put("mockTests", mockTests);
            map.put("assessments", assessments);
            map.put("codingChallenges", codingChallenges);
            map.put("mockInterviews", mockInterviews);
            map.put("courseEnrollments", courseEnrollments);
            map.put("totalActivity", totalActivity());
            return map;
        }

        Map<String, Object> summaryMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("planAccessCount", planAccessCount);
            map.put("mockTests", mockTests);
            map.put("assessments", assessments);
            map.put("codingChallenges", codingChallenges);
            map.put("mockInterviews", mockInterviews);
            map.put("courseEnrollments", courseEnrollments);
            map.put("totalActivity", totalActivity());
            return map;
        }

        int totalActivity() {
            return planAccessCount + mockTests + assessments + codingChallenges + mockInterviews + courseEnrollments;
        }
    }
}