package com.vidhuratech.jobs.certificate.repository;

import com.vidhuratech.jobs.certificate.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, String> {
}