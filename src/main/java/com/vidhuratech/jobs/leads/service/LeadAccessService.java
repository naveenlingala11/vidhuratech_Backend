package com.vidhuratech.jobs.leads.service;

import com.vidhuratech.jobs.leads.entity.LeadAccess;
import com.vidhuratech.jobs.leads.repository.LeadAccessRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeadAccessService {

    private final LeadAccessRepository repo;

    // 🔓 check access
    public boolean hasAccess(String phone) {
        return repo.findByPhone(phone)
                .map(LeadAccess::isAccess)
                .orElse(false);
    }

    // 🔥 admin grant access
    @Transactional
    public void grantAccess(String phone) {
        LeadAccess access = repo.findByPhone(phone)
                .orElseGet(LeadAccess::new);

        access.setPhone(phone);
        access.setAccess(true);

        repo.save(access);
    }
}