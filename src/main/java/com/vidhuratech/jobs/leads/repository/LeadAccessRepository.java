package com.vidhuratech.jobs.leads.repository;

import com.vidhuratech.jobs.leads.entity.LeadAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadAccessRepository extends JpaRepository<LeadAccess, Long> {

    Optional<LeadAccess> findByPhone(String phone);
}
