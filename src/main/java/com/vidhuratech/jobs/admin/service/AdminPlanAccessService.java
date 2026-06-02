package com.vidhuratech.jobs.admin.service;

import com.vidhuratech.jobs.leads.repository.LeadRepository;
import com.vidhuratech.jobs.plans.entity.PlanAccessGrant;
import com.vidhuratech.jobs.plans.repository.PlanAccessGrantRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminPlanAccessService {

    private final PlanAccessGrantRepository repo;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;

    public List<Map<String, Object>> list(String search) {
        String term = search == null ? "" : search.trim().toLowerCase();

        return repo.findAll()
                .stream()
                .filter(grant -> term.isBlank()
                        || text(grant.getBuyerName()).contains(term)
                        || text(grant.getBuyerEmail()).contains(term)
                        || text(grant.getBuyerPhone()).contains(term)
                        || text(grant.getPlanCode()).contains(term)
                        || text(grant.getStatus()).contains(term))
                .sorted((a, b) -> safeDate(b.getCreatedAt()).compareTo(safeDate(a.getCreatedAt())))
                .limit(300)
                .map(this::mapGrant)
                .toList();
    }

    public Map<String, Object> grant(Map<String, Object> payload) {
        String email = read(payload, "email");

        if (email.isBlank()) {
            throw new RuntimeException("User email is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        LocalDateTime now = LocalDateTime.now();

        PlanAccessGrant grant = PlanAccessGrant.builder()
                .userId(user.getId())
                .invoiceId("ADMIN-" + System.currentTimeMillis())
                .planCode(read(payload, "planCode").isBlank() ? "ADMIN_CUSTOM" : read(payload, "planCode"))
                .planName(read(payload, "planName").isBlank() ? "Admin Custom Access" : read(payload, "planName"))
                .buyerName(user.getName())
                .buyerEmail(user.getEmail())
                .buyerPhone(user.getPhone())
                .amount(0.0)
                .status(read(payload, "status").isBlank() ? "ACTIVE" : read(payload, "status").toUpperCase())
                .accessCourses(bool(payload, "accessCourses"))
                .accessMockTests(bool(payload, "accessMockTests"))
                .accessInterviews(bool(payload, "accessInterviews"))
                .accessNotes(bool(payload, "accessNotes"))
                .accessMaterials(bool(payload, "accessMaterials"))
                .accessVideos(bool(payload, "accessVideos"))
                .accessLiveClasses(bool(payload, "accessLiveClasses"))
                .accessPracticeCompanies(bool(payload, "accessPracticeCompanies"))
                .accessPremiumChallenges(bool(payload, "accessPremiumChallenges"))
                .companyLimit(number(payload, "companyLimit", 999))
                .startsAt(now)
                .expiresAt(now.plusDays(number(payload, "durationDays", 30)))
                .createdAt(now)
                .build();

        return mapGrant(repo.save(grant));
    }

    public Map<String, Object> update(Long id, Map<String, Object> payload) {
        PlanAccessGrant grant = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Access grant not found"));

        String planCode = read(payload, "planCode");
        String planName = read(payload, "planName");
        String status = read(payload, "status");

        if (!planCode.isBlank()) grant.setPlanCode(planCode);
        if (!planName.isBlank()) grant.setPlanName(planName);
        if (!status.isBlank()) grant.setStatus(status.toUpperCase());

        grant.setAccessCourses(bool(payload, "accessCourses"));
        grant.setAccessMockTests(bool(payload, "accessMockTests"));
        grant.setAccessInterviews(bool(payload, "accessInterviews"));
        grant.setAccessNotes(bool(payload, "accessNotes"));
        grant.setAccessMaterials(bool(payload, "accessMaterials"));
        grant.setAccessVideos(bool(payload, "accessVideos"));
        grant.setAccessLiveClasses(bool(payload, "accessLiveClasses"));
        grant.setAccessPracticeCompanies(bool(payload, "accessPracticeCompanies"));
        grant.setAccessPremiumChallenges(bool(payload, "accessPremiumChallenges"));
        grant.setCompanyLimit(number(payload, "companyLimit", grant.getCompanyLimit() == null ? 999 : grant.getCompanyLimit()));

        Object duration = payload.get("durationDays");
        if (duration != null && !String.valueOf(duration).isBlank()) {
            grant.setExpiresAt(LocalDateTime.now().plusDays(number(payload, "durationDays", 30)));
        }

        return mapGrant(repo.save(grant));
    }

    public void revoke(Long id) {
        PlanAccessGrant grant = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Access grant not found"));

        grant.setStatus("REVOKED");
        repo.save(grant);
    }

    private Map<String, Object> mapGrant(PlanAccessGrant grant) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", grant.getId());
        map.put("userId", grant.getUserId());
        map.put("invoiceId", grant.getInvoiceId());
        map.put("planCode", grant.getPlanCode());
        map.put("planName", grant.getPlanName());
        map.put("buyerName", grant.getBuyerName());
        map.put("buyerEmail", grant.getBuyerEmail());
        map.put("buyerPhone", grant.getBuyerPhone());
        map.put("amount", grant.getAmount());
        map.put("status", grant.getStatus());

        map.put("accessCourses", Boolean.TRUE.equals(grant.getAccessCourses()));
        map.put("accessMockTests", Boolean.TRUE.equals(grant.getAccessMockTests()));
        map.put("accessInterviews", Boolean.TRUE.equals(grant.getAccessInterviews()));
        map.put("accessNotes", Boolean.TRUE.equals(grant.getAccessNotes()));
        map.put("accessMaterials", Boolean.TRUE.equals(grant.getAccessMaterials()));
        map.put("accessVideos", Boolean.TRUE.equals(grant.getAccessVideos()));
        map.put("accessLiveClasses", Boolean.TRUE.equals(grant.getAccessLiveClasses()));
        map.put("accessPracticeCompanies", Boolean.TRUE.equals(grant.getAccessPracticeCompanies()));
        map.put("accessPremiumChallenges", Boolean.TRUE.equals(grant.getAccessPremiumChallenges()));

        map.put("companyLimit", grant.getCompanyLimit());
        map.put("startsAt", grant.getStartsAt());
        map.put("expiresAt", grant.getExpiresAt());
        map.put("createdAt", grant.getCreatedAt());

        return map;
    }

    public List<Map<String, Object>> people(String search) {
        String term = search == null ? "" : search.trim().toLowerCase();
        List<Map<String, Object>> rows = new ArrayList<>();

        userRepository.findAll().forEach(user -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("source", "USER");
            row.put("id", user.getId());
            row.put("name", user.getName());
            row.put("email", user.getEmail());
            row.put("phone", user.getPhone());
            row.put("role", user.getRole());
            row.put("active", user.getActive());
            row.put("createdAt", user.getCreatedAt());
            rows.add(row);
        });

        leadRepository.findAll().forEach(lead -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("source", "LEAD");
            row.put("id", lead.getId());
            row.put("name", lead.getName());
            row.put("email", lead.getEmail());
            row.put("phone", lead.getPhone());
            row.put("role", "LEAD");
            row.put("active", !Boolean.TRUE.equals(lead.getDeleted()));
            row.put("createdAt", lead.getCreatedAt());
            rows.add(row);
        });

        return rows.stream()
                .filter(row -> term.isBlank() || String.valueOf(row).toLowerCase().contains(term))
                .limit(500)
                .toList();
    }

    private String text(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private LocalDateTime safeDate(LocalDateTime date) {
        return date == null ? LocalDateTime.MIN : date;
    }

    private String read(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private boolean bool(Map<String, Object> payload, String key) {
        Object value = payload.get(key);

        if (value == null) return false;
        if (value instanceof Boolean bool) return bool;

        return Boolean.parseBoolean(String.valueOf(value));
    }

    private int number(Map<String, Object> payload, String key, int fallback) {
        Object value = payload.get(key);

        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }

        return Integer.parseInt(String.valueOf(value));
    }
}