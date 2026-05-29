package com.vidhuratech.jobs.certificate.service;

import com.vidhuratech.jobs.certificate.entity.Certificate;
import com.vidhuratech.jobs.certificate.repository.CertificateRepository;
import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class CertificateService {

    private final CertificateRepository repo;
    private final ActivityNotificationService notificationService;
    private final UserRepository userRepository;

    public CertificateService(CertificateRepository repo, ActivityNotificationService notificationService, UserRepository userRepository) {
        this.repo = repo;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    public Certificate save(Certificate c) {

        // 🔥 GENERATE UNIQUE ID
        c.setId("VT-" + UUID.randomUUID().toString().substring(0, 8));

        c.setIssuedAt(LocalDateTime.now());
        Certificate saved = repo.save(c);
        userRepository.findByEmail(saved.getEmail()).ifPresent(student ->
                notificationService.notifyStudent(
                        student,
                        "Certificate issued",
                        "Your certificate is ready for " + saved.getCourse(),
                        "CERTIFICATE_ISSUED",
                        "/dashboard/student/certificates"
                )
        );

        notificationService.notifyAdmins(
                "Certificate issued",
                "Certificate issued to " + saved.getName(),
                "CERTIFICATE_ISSUED",
                "/dashboard/admin/certificates"
        );
        return saved;
    }

    public Optional<Certificate> get(String id) {
        return repo.findById(id);
    }
}