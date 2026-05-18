package com.vidhuratech.jobs.certificate.controller;

import com.vidhuratech.jobs.certificate.dto.StudentCertificateRequest;
import com.vidhuratech.jobs.certificate.entity.Certificate;
import com.vidhuratech.jobs.certificate.repository.CertificateRepository;
import com.vidhuratech.jobs.certificate.service.CertificateService;
import com.vidhuratech.jobs.common.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
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
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public CertificateController(CertificateService service, CertificateRepository repo, EmailService emailService) {
        this.service = service;
        this.repo = repo;
        this.emailService = emailService;
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

    @GetMapping("/student")
    public ResponseEntity<?> getStudentCertificates(@RequestParam(required = false) String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.ok(repo.findAll());
        }

        return ResponseEntity.ok(repo.findByEmail(email));
    }

    @PostMapping("/student/generate")
    public ResponseEntity<?> generateStudentCertificate(@RequestBody StudentCertificateRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        if (req.getCourse() == null || req.getCourse().isBlank()) {
            return ResponseEntity.badRequest().body("Course is required");
        }

        return repo.findByEmailAndCourse(req.getEmail(), req.getCourse())
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    Certificate c = Certificate.builder()
                            .name(req.getName())
                            .email(req.getEmail())
                            .mobile(req.getMobile())
                            .course(req.getCourse())
                            .build();

                    return ResponseEntity.ok(service.save(c));
                });
    }

    @PostMapping("/{id}/send-email")
    public ResponseEntity<?> sendCertificateEmail(@PathVariable String id) {
        Certificate c = repo.findById(id).orElseThrow();

        if (c.getEmail() == null || c.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Certificate email is missing");
        }

        String certificateUrl = frontendUrl + "/certificate/" + c.getId();

        String html = """
            <div style="font-family:Arial,sans-serif;background:#f8fafc;padding:24px">
              <div style="max-width:620px;margin:auto;background:white;border-radius:14px;padding:28px;border:1px solid #e5e7eb">
                <h2 style="color:#0f172a;margin-top:0">Your Certificate is Ready</h2>

                <p style="color:#475569;font-size:15px;line-height:1.7">
                  Congratulations <b>%s</b>,
                </p>

                <p style="color:#475569;font-size:15px;line-height:1.7">
                  Your certificate for <b>%s</b> has been generated successfully.
                </p>

                <div style="background:#f8fafc;border:1px solid #e5e7eb;border-radius:10px;padding:14px;margin:20px 0">
                  <p style="margin:0;color:#64748b;font-size:13px">Certificate ID</p>
                  <h3 style="margin:6px 0 0;color:#111827">%s</h3>
                </div>

                <a href="%s"
                   style="display:inline-block;background:#111827;color:white;text-decoration:none;padding:13px 18px;border-radius:10px;font-weight:700">
                  View Certificate
                </a>

                <p style="color:#64748b;font-size:13px;margin-top:24px">
                  Regards,<br>
                  Vidhura Tech Team
                </p>
              </div>
            </div>
            """.formatted(
                c.getName(),
                c.getCourse(),
                c.getId(),
                certificateUrl
        );

        emailService.sendHtmlEmail(
                c.getEmail(),
                "Your Vidhura Tech Certificate is Ready",
                html
        );

        return ResponseEntity.ok("Certificate email sent to " + c.getEmail());
    }
}