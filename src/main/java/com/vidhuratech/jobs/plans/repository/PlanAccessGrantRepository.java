package com.vidhuratech.jobs.plans.repository;

import com.vidhuratech.jobs.plans.entity.PlanAccessGrant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PlanAccessGrantRepository extends JpaRepository<PlanAccessGrant, Long> {

    List<PlanAccessGrant> findByUserIdAndStatusAndExpiresAtAfter(
            Long userId,
            String status,
            LocalDateTime now
    );

    List<PlanAccessGrant> findByBuyerEmailIgnoreCaseAndStatusAndExpiresAtAfter(
            String email,
            String status,
            LocalDateTime now
    );

    boolean existsByInvoiceId(String invoiceId);
}