package com.vidhuratech.jobs.admin.service;

import com.vidhuratech.jobs.leads.entity.Lead;
import com.vidhuratech.jobs.leads.repository.LeadRepository;
import com.vidhuratech.jobs.plans.entity.DiscountControl;
import com.vidhuratech.jobs.plans.entity.PlanPricingControl;
import com.vidhuratech.jobs.plans.entity.ProjectAccessControl;
import com.vidhuratech.jobs.plans.repository.DiscountControlRepository;
import com.vidhuratech.jobs.plans.repository.PlanPricingControlRepository;
import com.vidhuratech.jobs.plans.repository.ProjectAccessControlRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminCommercialControlService {

    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final PlanPricingControlRepository pricingRepository;
    private final DiscountControlRepository discountRepository;
    private final ProjectAccessControlRepository projectControlRepository;

    public List<Map<String, Object>> people(String search) {
        String term = search == null ? "" : search.trim().toLowerCase();

        Map<String, Map<String, Object>> merged = new LinkedHashMap<>();

        for (User user : userRepository.findAll()) {
            String key = personKey(user.getEmail(), user.getPhone(), "USER:" + user.getId());

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("source", "USER");
            row.put("id", user.getId());
            row.put("name", safe(user.getName()));
            row.put("email", safe(user.getEmail()));
            row.put("phone", safe(user.getPhone()));
            row.put("role", user.getRole() == null ? "USER" : String.valueOf(user.getRole()));
            row.put("active", Boolean.TRUE.equals(user.getActive()));
            row.put("createdAt", user.getCreatedAt());

            merged.put(key, row);
        }

        for (Lead lead : leadRepository.findAll()) {
            String key = personKey(lead.getEmail(), lead.getPhone(), "LEAD:" + lead.getId());

            if (merged.containsKey(key)) {
                Map<String, Object> existing = merged.get(key);
                String currentSource = String.valueOf(existing.get("source"));
                if (!currentSource.contains("LEAD")) {
                    existing.put("source", currentSource + "+LEAD");
                }

                if (isBlank(String.valueOf(existing.get("name")))) {
                    existing.put("name", safe(lead.getName()));
                }

                if (isBlank(String.valueOf(existing.get("phone")))) {
                    existing.put("phone", safe(lead.getPhone()));
                }

                continue;
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("source", "LEAD");
            row.put("id", lead.getId());
            row.put("name", safe(lead.getName()));
            row.put("email", safe(lead.getEmail()));
            row.put("phone", safe(lead.getPhone()));
            row.put("role", "LEAD");
            row.put("active", !Boolean.TRUE.equals(lead.getDeleted()));
            row.put("createdAt", lead.getCreatedAt());

            merged.put(key, row);
        }

        return merged.values()
                .stream()
                .filter(row -> term.isBlank() || String.valueOf(row).toLowerCase().contains(term))
                .sorted((a, b) -> compareCreatedAt(b.get("createdAt"), a.get("createdAt")))
                .limit(500)
                .toList();
    }

    public List<Map<String, Object>> pricing() {
        return pricingRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(PlanPricingControl::getId))
                .map(this::mapPricing)
                .toList();
    }

    @CacheEvict(value = "pricing_plans", allEntries = true)
    public Map<String, Object> updatePricing(String planCode, Map<String, Object> payload) {
        PlanPricingControl plan = pricingRepository.findByPlanCodeIgnoreCase(planCode)
                .orElseGet(() -> PlanPricingControl.builder()
                        .planCode(planCode.toUpperCase())
                        .planName(planCode.toUpperCase())
                        .price(0.0)
                        .compareAtPrice(null)
                        .durationDays(30)
                        .companyLimit(999)
                        .highlighted(false)
                        .active(true)
                        .updatedAt(LocalDateTime.now())
                        .build());

        plan.setPlanName(read(payload, "planName").isBlank() ? plan.getPlanName() : read(payload, "planName"));
        plan.setPrice(decimal(payload, "price", plan.getPrice() == null ? 0.0 : plan.getPrice()));
        plan.setCompareAtPrice(decimalNullable(payload, "compareAtPrice", plan.getCompareAtPrice()));
        plan.setDurationDays(number(payload, "durationDays", plan.getDurationDays() == null ? 30 : plan.getDurationDays()));
        plan.setCompanyLimit(number(payload, "companyLimit", plan.getCompanyLimit() == null ? 999 : plan.getCompanyLimit()));
        plan.setHighlighted(bool(payload, "highlighted", Boolean.TRUE.equals(plan.getHighlighted())));
        plan.setActive(bool(payload, "active", Boolean.TRUE.equals(plan.getActive())));
        plan.setUpdatedAt(LocalDateTime.now());

        return mapPricing(pricingRepository.save(plan));
    }

    public List<Map<String, Object>> discounts() {
        return discountRepository.findAll()
                .stream()
                .sorted((a, b) -> safeDate(b.getCreatedAt()).compareTo(safeDate(a.getCreatedAt())))
                .map(this::mapDiscount)
                .toList();
    }

    public Map<String, Object> saveDiscount(Map<String, Object> payload) {
        String code = read(payload, "code").toUpperCase();

        if (code.isBlank()) {
            throw new RuntimeException("Discount code is required");
        }

        DiscountControl discount = discountRepository.findByCodeIgnoreCase(code)
                .orElseGet(() -> DiscountControl.builder()
                        .code(code)
                        .usedCount(0)
                        .createdAt(LocalDateTime.now())
                        .build());

        discount.setCode(code);
        discount.setTitle(read(payload, "title").isBlank() ? code : read(payload, "title"));
        discount.setDiscountType(read(payload, "discountType").isBlank() ? "PERCENT" : read(payload, "discountType").toUpperCase());
        discount.setDiscountValue(decimal(payload, "discountValue", 0.0));
        discount.setPlanCode(read(payload, "planCode").isBlank() ? null : read(payload, "planCode").toUpperCase());
        discount.setMaxUses(numberNullable(payload, "maxUses", discount.getMaxUses()));
        discount.setActive(bool(payload, "active", true));
        discount.setUpdatedAt(LocalDateTime.now());

        return mapDiscount(discountRepository.save(discount));
    }

    public List<Map<String, Object>> projectControls() {
        return projectControlRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ProjectAccessControl::getControlKey))
                .map(this::mapProjectControl)
                .toList();
    }

    public Map<String, Object> updateProjectControl(String key, Map<String, Object> payload) {
        ProjectAccessControl control = projectControlRepository.findByControlKeyIgnoreCase(key)
                .orElseThrow(() -> new RuntimeException("Project control not found: " + key));

        control.setEnabled(bool(payload, "enabled", Boolean.TRUE.equals(control.getEnabled())));
        control.setUpdatedAt(LocalDateTime.now());

        return mapProjectControl(projectControlRepository.save(control));
    }

    private Map<String, Object> mapPricing(PlanPricingControl plan) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", plan.getId());
        map.put("planCode", plan.getPlanCode());
        map.put("planName", plan.getPlanName());
        map.put("price", plan.getPrice());
        map.put("compareAtPrice", plan.getCompareAtPrice());
        map.put("durationDays", plan.getDurationDays());
        map.put("companyLimit", plan.getCompanyLimit());
        map.put("highlighted", Boolean.TRUE.equals(plan.getHighlighted()));
        map.put("active", Boolean.TRUE.equals(plan.getActive()));
        map.put("updatedAt", plan.getUpdatedAt());
        return map;
    }

    private Map<String, Object> mapDiscount(DiscountControl discount) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", discount.getId());
        map.put("code", discount.getCode());
        map.put("title", discount.getTitle());
        map.put("discountType", discount.getDiscountType());
        map.put("discountValue", discount.getDiscountValue());
        map.put("planCode", discount.getPlanCode());
        map.put("maxUses", discount.getMaxUses());
        map.put("usedCount", discount.getUsedCount());
        map.put("startsAt", discount.getStartsAt());
        map.put("expiresAt", discount.getExpiresAt());
        map.put("active", Boolean.TRUE.equals(discount.getActive()));
        map.put("createdAt", discount.getCreatedAt());
        map.put("updatedAt", discount.getUpdatedAt());
        return map;
    }

    private Map<String, Object> mapProjectControl(ProjectAccessControl control) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", control.getId());
        map.put("controlKey", control.getControlKey());
        map.put("label", control.getLabel());
        map.put("description", control.getDescription());
        map.put("enabled", Boolean.TRUE.equals(control.getEnabled()));
        map.put("updatedAt", control.getUpdatedAt());
        return map;
    }

    private String personKey(String email, String phone, String fallback) {
        if (email != null && !email.isBlank()) {
            return "EMAIL:" + email.trim().toLowerCase();
        }

        if (phone != null && !phone.isBlank()) {
            return "PHONE:" + phone.replaceAll("\\D", "");
        }

        return fallback;
    }

    private int compareCreatedAt(Object a, Object b) {
        return safeDate((LocalDateTime) a).compareTo(safeDate((LocalDateTime) b));
    }

    private LocalDateTime safeDate(LocalDateTime date) {
        return date == null ? LocalDateTime.MIN : date;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    private String read(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private boolean bool(Map<String, Object> payload, String key, boolean fallback) {
        Object value = payload.get(key);

        if (value == null) return fallback;
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

    private Integer numberNullable(Map<String, Object> payload, String key, Integer fallback) {
        Object value = payload.get(key);

        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }

        return Integer.parseInt(String.valueOf(value));
    }

    private Double decimal(Map<String, Object> payload, String key, Double fallback) {
        Object value = payload.get(key);

        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }

        return Double.parseDouble(String.valueOf(value));
    }

    private Double decimalNullable(Map<String, Object> payload, String key, Double fallback) {
        Object value = payload.get(key);

        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }

        return Double.parseDouble(String.valueOf(value));
    }
}