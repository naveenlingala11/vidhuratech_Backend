package com.vidhuratech.jobs.certificate.controller;

import com.vidhuratech.jobs.certificate.entity.Certificate;
import com.vidhuratech.jobs.certificate.repository.CertificateRepository;
import com.vidhuratech.jobs.certificate.service.CertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/certificates")
@CrossOrigin
public class CertificateController {

    private final CertificateService service;
    private final CertificateRepository repo;

    public CertificateController(CertificateService service, CertificateRepository repo) {
        this.service = service;
        this.repo = repo;
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

    @PostMapping("/bulk")
    public List<Certificate> bulk(@RequestBody List<Certificate> list) {

        return list.stream().map(c -> {
            c.setId("VT-" + UUID.randomUUID().toString().substring(0, 8));
            c.setIssuedAt(LocalDateTime.now());
            return repo.save(c);
        }).toList();
    }

    @GetMapping
    public List<Certificate> getAll() {
        return repo.findAll();
    }

    @PutMapping("/{id}/remarks")
    public Certificate updateRemarks(@PathVariable String id, @RequestBody String remarks) {
        Certificate c = repo.findById(id).orElseThrow();
        c.setRemarks(remarks);
        return repo.save(c);
    }
}