package com.vidhuratech.jobs.certificate.controller;

import com.vidhuratech.jobs.certificate.entity.Certificate;
import com.vidhuratech.jobs.certificate.service.CertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/certificates")
@CrossOrigin
public class CertificateController {

    private final CertificateService service;

    public CertificateController(CertificateService service) {
        this.service = service;
    }

    // 🔥 SAVE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Certificate c) {
        return ResponseEntity.ok(service.save(c));
    }

    // 🔥 GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        return service.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}