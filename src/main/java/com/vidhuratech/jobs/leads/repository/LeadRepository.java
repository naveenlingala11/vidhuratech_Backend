package com.vidhuratech.jobs.leads.repository;

import com.vidhuratech.jobs.leads.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    Optional<Lead> findByPhone(String phone);

    @Query("""
SELECT l FROM Lead l 
WHERE l.deleted = false 
AND (LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%')) 
     OR l.phone LIKE CONCAT('%', :search, '%'))
""")
    Page<Lead> searchLeads(String search, Pageable pageable);

    long countByStatus(String status);
    Page<Lead> findByDeletedFalse(Pageable pageable);

    List<Lead> findByDeletedTrue();

    Page<Lead> findByDeletedTrue(Pageable pageable);

    List<Lead> findByDeletedTrueAndDeletedAtBefore(LocalDateTime time);

    List<Lead> findTop5ByPhoneContainingAndDeletedFalseOrderByCreatedAtDesc(String phone);

    @Query("SELECT COUNT(l) > 0 FROM Lead l WHERE l.phone = :phone AND l.createdAt >= :time AND l.deleted = false")
    boolean existsRecentLead(String phone, LocalDateTime time);

    // FIXED
    List<Lead> findAllByPhoneOrderByCreatedAtDesc(String phone);
}