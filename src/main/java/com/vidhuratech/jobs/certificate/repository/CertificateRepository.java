package com.vidhuratech.jobs.certificate.repository;

import com.vidhuratech.jobs.certificate.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, String> {

    List<Certificate> findByEmail(String email);

    Optional<Certificate> findByEmailAndCourse(String email, String course);

    boolean existsByEmailAndCourse(String email, String course);
}