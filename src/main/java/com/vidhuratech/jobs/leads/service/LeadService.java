package com.vidhuratech.jobs.leads.service;

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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository repo;

    public void saveLead(Lead lead) {
        if (lead == null) {
            throw new RuntimeException("Invalid request");
        }

        String rawPhone = lead.getPhone() == null ? "" : lead.getPhone().replaceAll("\\D", "");
        if (rawPhone.isBlank()) {
            throw new RuntimeException("Phone is required");
        }

        if (rawPhone.length() > 15) {
            rawPhone = rawPhone.substring(rawPhone.length() - 15);
        }

        lead.setPhone(rawPhone);

        if (lead.getName() != null) lead.setName(lead.getName().trim());
        if (lead.getEmail() != null) lead.setEmail(lead.getEmail().trim());
        if (lead.getCourse() != null) lead.setCourse(lead.getCourse().trim());
        if (lead.getCity() != null) lead.setCity(lead.getCity().trim());
        if (lead.getMessage() != null) lead.setMessage(lead.getMessage().trim());
        if (lead.getSource() != null) lead.setSource(lead.getSource().trim());

        Optional<Lead> existing = repo.findByPhone(rawPhone);

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

        if (lead.getDeleted() == null) lead.setDeleted(false);
        repo.save(lead);
    }
    public void updateStatus(String phone, String status) {
        Lead lead = repo.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Lead not found: " + phone));

        lead.setStatus(status);
        repo.save(lead);
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

        Lead lead = repo.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Lead not found: " + phone));

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