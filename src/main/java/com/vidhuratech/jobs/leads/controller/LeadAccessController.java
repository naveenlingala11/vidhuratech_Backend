package com.vidhuratech.jobs.leads.controller;

import com.vidhuratech.jobs.leads.service.LeadAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/access")
@RequiredArgsConstructor
@CrossOrigin("*")
public class LeadAccessController {

    private final LeadAccessService service;

    // 🔍 CHECK ACCESS (Frontend use)
    @GetMapping("/check")
    public Map<String, Object> check(@RequestParam String phone) {
        boolean access = service.hasAccess(phone);
        return Map.of("access", access);
    }

    // 🔥 ADMIN GRANT ACCESS
    @PostMapping("/grant")
    public void grant(@RequestParam String phone) {
        service.grantAccess(phone);
    }
}
