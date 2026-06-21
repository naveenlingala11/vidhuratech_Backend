package com.vidhuratech.jobs.mentor.service;

import com.vidhuratech.jobs.mentor.entity.MentorBookingRequest;
import com.vidhuratech.jobs.mentor.entity.MentorStudentRelation;
import com.vidhuratech.jobs.mentor.repository.MentorBookingRequestRepository;
import com.vidhuratech.jobs.mentor.repository.MentorStudentRelationRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorBookingService {

    private final MentorBookingRequestRepository bookingRepo;
    private final MentorStudentRelationRepository relationRepo;
    private final UserRepository userRepo;

    // ── Student submits a booking request ──
    @Transactional
    public Map<String, Object> createBookingRequest(Long studentId, Long mentorId, String topic,
                                                     String message, String preferredPlan) {
        // Check if pending request already exists
        if (bookingRepo.existsByMentorIdAndStudentIdAndStatus(mentorId, studentId, "PENDING")) {
            throw new RuntimeException("You already have a pending booking request with this mentor");
        }

        // Check if student is already in the mentor's active roster
        if (relationRepo.existsByMentorIdAndStudentId(mentorId, studentId)) {
            throw new RuntimeException("You are already active in this mentor's roster");
        }

        User student = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        User mentor = userRepo.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        MentorBookingRequest req = new MentorBookingRequest();
        req.setStudent(student);
        req.setMentor(mentor);
        req.setStudentName(student.getName());
        req.setStudentPhone(student.getPhone());
        req.setStudentEmail(student.getEmail());
        req.setTopic(topic);
        req.setMessage(message);
        req.setPreferredPlan(preferredPlan != null ? preferredPlan : "HOURLY");
        req.setStatus("PENDING");

        bookingRepo.save(req);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Booking request sent successfully! The mentor will review your request.");
        result.put("requestId", req.getId());
        return result;
    }

    // ── Mentor views all booking requests ──
    @Transactional(readOnly = true)
    public Map<String, Object> getMentorBookingRequests(Long mentorId) {
        Map<String, Object> result = new HashMap<>();

        List<MentorBookingRequest> all = bookingRepo.findAllByMentorId(mentorId);
        long pending = bookingRepo.countByMentorIdAndStatus(mentorId, "PENDING");
        long accepted = bookingRepo.countByMentorIdAndStatus(mentorId, "ACCEPTED");
        long rejected = bookingRepo.countByMentorIdAndStatus(mentorId, "REJECTED");

        result.put("totalRequests", all.size());
        result.put("pendingCount", pending);
        result.put("acceptedCount", accepted);
        result.put("rejectedCount", rejected);

        List<Map<String, Object>> requests = all.stream().map(b -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("studentName", b.getStudentName());
            m.put("studentPhone", b.getStudentPhone());
            m.put("studentEmail", b.getStudentEmail());
            m.put("studentAvatar", b.getStudentName() != null ? b.getStudentName().substring(0, 1).toUpperCase() : "S");
            m.put("topic", b.getTopic());
            m.put("message", b.getMessage());
            m.put("preferredPlan", b.getPreferredPlan());
            m.put("status", b.getStatus());
            m.put("mentorNote", b.getMentorNote());
            m.put("createdAt", b.getCreatedAt());
            m.put("studentId", b.getStudent().getId());
            return m;
        }).collect(Collectors.toList());

        result.put("requests", requests);
        return result;
    }

    // ── Mentor accepts a booking request → creates MentorStudentRelation ──
    @Transactional
    public Map<String, Object> acceptBookingRequest(Long mentorId, Long requestId, String mentorNote) {
        MentorBookingRequest req = bookingRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Booking request not found"));

        if (!req.getMentor().getId().equals(mentorId)) {
            throw new RuntimeException("Unauthorized: This booking request belongs to a different mentor");
        }

        if (!"PENDING".equals(req.getStatus())) {
            throw new RuntimeException("This request has already been " + req.getStatus().toLowerCase());
        }

        if (mentorNote == null || mentorNote.trim().isEmpty()) {
            throw new RuntimeException("Response message note is required");
        }

        req.setStatus("ACCEPTED");
        req.setMentorNote(mentorNote.trim());
        bookingRepo.save(req);

        // Auto-create MentorStudentRelation if not exists
        if (!relationRepo.existsByMentorIdAndStudentId(mentorId, req.getStudent().getId())) {
            MentorStudentRelation relation = new MentorStudentRelation();
            relation.setMentor(req.getMentor());
            relation.setStudent(req.getStudent());
            relation.setStatus("ACTIVE");
            relationRepo.save(relation);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Booking request accepted! Student has been added as your mentee.");
        return result;
    }

    // ── Mentor rejects a booking request ──
    @Transactional
    public Map<String, Object> rejectBookingRequest(Long mentorId, Long requestId, String mentorNote) {
        MentorBookingRequest req = bookingRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Booking request not found"));

        if (!req.getMentor().getId().equals(mentorId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (!"PENDING".equals(req.getStatus())) {
            throw new RuntimeException("This request has already been " + req.getStatus().toLowerCase());
        }

        if (mentorNote == null || mentorNote.trim().isEmpty()) {
            throw new RuntimeException("Response message note is required");
        }

        req.setStatus("REJECTED");
        req.setMentorNote(mentorNote.trim());
        bookingRepo.save(req);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Booking request declined.");
        return result;
    }

    // ── Student views their booking requests ──
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudentBookingRequests(Long studentId) {
        List<MentorBookingRequest> requests = bookingRepo.findAllByStudentId(studentId);
        return requests.stream().map(b -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("mentorName", b.getMentor().getName());
            m.put("mentorAvatar", b.getMentor().getName().substring(0, 1).toUpperCase());
            m.put("mentorId", b.getMentor().getId());
            m.put("topic", b.getTopic());
            m.put("preferredPlan", b.getPreferredPlan());
            m.put("status", b.getStatus());
            m.put("mentorNote", b.getMentorNote());
            m.put("createdAt", b.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
    }
}
