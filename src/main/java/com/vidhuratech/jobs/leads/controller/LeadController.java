package com.vidhuratech.jobs.leads.controller;

import com.vidhuratech.jobs.leads.entity.Lead;
import com.vidhuratech.jobs.leads.repository.LeadRepository;
import com.vidhuratech.jobs.leads.service.LeadAccessService;
import com.vidhuratech.jobs.leads.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@CrossOrigin("*")
public class LeadController {

    private final LeadService service;
    private final LeadAccessService accessService;
    private final LeadRepository repository;

    // ✅ SAVE FORM
    @PostMapping("/save")
    public void save(@RequestBody Lead lead) {
        service.saveLead(lead);
    }

    // 🔥 ADMIN UPDATE STATUS
    @PostMapping("/status")
    public void updateStatus(@RequestParam String phone,
                             @RequestParam String status) {

        service.updateStatus(phone, status);

        // 🔥 IF JOINED → GRANT ACCESS
        if ("Joined".equalsIgnoreCase(status)) {
            accessService.grantAccess(phone);
        }
    }

    @GetMapping("/all")
    public List<Lead> getAllLeads() {
        return service.getAllLeads();
    }

    @GetMapping
    public Page<Lead> getLeads(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return service.getLeads(search, page, size, sortBy, direction);
    }

    @GetMapping("/analytics")
    public Map<String, Object> analytics() {

        long total = repository.count();
        long newCount = repository.countByStatus("New");
        long contacted = repository.countByStatus("Contacted");
        long joined = repository.countByStatus("Joined");

        return Map.of(
                "total", total,
                "new", newCount,
                "contacted", contacted,
                "joined", joined
        );
    }

    // ❌ move to bin
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.moveToBin(id);
    }

    // ♻ restore
    @PutMapping("/restore/{id}")
    public void restore(@PathVariable Long id) {
        service.restoreLead(id);
    }

    // 🗑 permanent delete
    @DeleteMapping("/permanent/{id}")
    public void deletePermanent(@PathVariable Long id) {
        service.deletePermanent(id);
    }

    // 📦 get bin data
    @GetMapping("/bin")
    public Page<Lead> getBin(@RequestParam int page, @RequestParam int size) {
        return service.getDeletedLeads(page, size);
    }

    @PostMapping("/followup")
    public void updateFollowUp(
            @RequestParam String phone,
            @RequestParam String date) {

        service.updateFollowUp(phone, date);
    }
}
