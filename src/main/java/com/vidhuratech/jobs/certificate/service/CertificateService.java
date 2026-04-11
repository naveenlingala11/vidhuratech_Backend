package com.vidhuratech.jobs.certificate.service;

import com.vidhuratech.jobs.certificate.entity.Certificate;
import com.vidhuratech.jobs.certificate.repository.CertificateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class CertificateService {

    private final CertificateRepository repo;

    public CertificateService(CertificateRepository repo) {
        this.repo = repo;
    }

    public Certificate save(Certificate c) {

        // 🔥 GENERATE UNIQUE ID
        c.setId("VT-" + UUID.randomUUID().toString().substring(0, 8));

        c.setIssuedAt(LocalDateTime.now());

        return repo.save(c);
    }

    public Optional<Certificate> get(String id) {
        return repo.findById(id);
    }
}