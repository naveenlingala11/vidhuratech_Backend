package com.vidhuratech.jobs.leads.service;

import com.vidhuratech.jobs.common.exception.AlreadyRegisteredException;
import com.vidhuratech.jobs.leads.entity.Lead;
import com.vidhuratech.jobs.leads.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository repo;
    public void saveLead(Lead lead) {
        if (lead == null) {
            throw new RuntimeException("Invalid request");
        }

        String rawPhone = cleanPhone(lead.getPhone());

        if (rawPhone.isBlank()) {
            throw new RuntimeException("Phone is required");
        }

        lead.setPhone(rawPhone);
        trimLeadFields(lead);

        Optional<Lead> existing = repo.findFirstByPhoneOrderByCreatedAtDesc(rawPhone);

        if (existing.isPresent()) {
            Lead old = existing.get();
            old.setName(lead.getName());
            old.setEmail(lead.getEmail());
            old.setCourse(lead.getCourse());
            old.setCity(lead.getCity());
            old.setMessage(lead.getMessage());
            old.setSource(lead.getSource());
            old.setDeleted(false);
            old.setDeletedAt(null);
            old.setCreatedAt(LocalDateTime.now());
            repo.save(old);
            return;
        }

        if (lead.getDeleted() == null) {
            lead.setDeleted(false);
        }

        repo.save(lead);
    }

    public void saveMockInterviewInterest(Map<String, Object> payload) {
        if (payload == null) {
            throw new RuntimeException("Invalid request");
        }

        String name = readText(payload, "name");
        String email = readText(payload, "email");
        String skills = readText(payload, "skills");
        String interested = readText(payload, "interested");
        String message = readText(payload, "message");

        if (name.isBlank()) {
            throw new RuntimeException("Name is required");
        }

        if (email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (skills.isBlank()) {
            throw new RuntimeException("Skills are required");
        }

        Lead lead = new Lead();
        lead.setName(name);
        lead.setEmail(email);
        lead.setPhone("");
        lead.setCourse("Mock Interview Interest");
        lead.setExperience(skills);
        lead.setStatus("New");
        lead.setSource("HOME_MOCK_INTERVIEW_INTEREST");
        lead.setDeleted(false);
        lead.setCreatedAt(LocalDateTime.now());
        lead.setMessage(
                "Interested: " + (interested.isBlank() ? "Not specified" : interested)
                        + "\nSkills: " + skills
                        + (message.isBlank() ? "" : "\nMessage: " + message)
        );

        repo.save(lead);
    }

    private String readText(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    public void savePublicPracticeLead(Lead lead) {
        if (lead == null) {
            throw new RuntimeException("Invalid request");
        }

        String rawPhone = cleanPhone(lead.getPhone());

        if (rawPhone.isBlank()) {
            throw new RuntimeException("Phone is required");
        }

        lead.setPhone(rawPhone);
        trimLeadFields(lead);

        Optional<Lead> existing = repo.findFirstByPhoneOrderByCreatedAtDesc(rawPhone);

        if (existing.isPresent()) {
            throw new AlreadyRegisteredException(
                    "Already a member. Please login with your credentials to continue practice."
            );
        }

        if (lead.getDeleted() == null) {
            lead.setDeleted(false);
        }

        repo.save(lead);
    }

    public void updateStatus(String phone, String status) {
        String cleanPhone = cleanPhone(phone);

        Lead lead = repo.findFirstByPhoneOrderByCreatedAtDesc(cleanPhone)
                .orElseThrow(() -> new RuntimeException("Lead not found: " + cleanPhone));

        lead.setStatus(status);
        repo.save(lead);
    }

    private String cleanPhone(String phone) {
        String rawPhone = phone == null ? "" : phone.replaceAll("\\D", "");

        if (rawPhone.length() > 15) {
            rawPhone = rawPhone.substring(rawPhone.length() - 15);
        }

        return rawPhone;
    }

    private void trimLeadFields(Lead lead) {
        if (lead.getName() != null) lead.setName(lead.getName().trim());
        if (lead.getEmail() != null) lead.setEmail(lead.getEmail().trim());
        if (lead.getCourse() != null) lead.setCourse(lead.getCourse().trim());
        if (lead.getCity() != null) lead.setCity(lead.getCity().trim());
        if (lead.getMessage() != null) lead.setMessage(lead.getMessage().trim());
        if (lead.getSource() != null) lead.setSource(lead.getSource().trim());
    }

    public List<Lead> getAllLeads() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Page<Lead> getLeads(String search, int page, int size, String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (search == null || search.isEmpty()) {
            return repo.findByDeletedFalse(pageable);
        }

        return repo.searchLeads(search, pageable);
    }

    public Page<Lead> getDeletedLeads(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("deletedAt").descending());

        return repo.findByDeletedTrue(pageable);
    }

    public void moveToBin(Long id) {
        Lead lead = repo.findById(id).orElseThrow();

        lead.setDeleted(true);
        lead.setDeletedAt(LocalDateTime.now());

        repo.save(lead);
    }

    public void restoreLead(Long id) {
        Lead lead = repo.findById(id).orElseThrow();

        lead.setDeleted(false);
        lead.setDeletedAt(null);

        repo.save(lead);
    }

    public void deletePermanent(Long id) {
        repo.deleteById(id);
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupBin() {

        LocalDateTime limit = LocalDateTime.now().minusDays(30);

        List<Lead> old = repo.findByDeletedTrueAndDeletedAtBefore(limit);

        repo.deleteAll(old);
    }

    public void updateFollowUp(String phone, String date) {
        String cleanPhone = cleanPhone(phone);

        Lead lead = repo.findFirstByPhoneOrderByCreatedAtDesc(cleanPhone)
                .orElseThrow(() -> new RuntimeException("Lead not found: " + cleanPhone));

        try {
            lead.setFollowUpDate(LocalDate.parse(date));
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format");
        }

        repo.save(lead);
    }

    public List<Lead> searchByPhone(String phone) {
        return repo.findTop5ByPhoneContainingAndDeletedFalseOrderByCreatedAtDesc(phone);
    }

}