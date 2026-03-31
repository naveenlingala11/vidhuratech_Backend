package com.vidhuratech.jobs.jobs.repository;

import com.vidhuratech.jobs.jobs.entity.ScraperConfigEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ScraperConfigRepository extends JpaRepository<ScraperConfigEntity, Long> {
    List<ScraperConfigEntity> findByActiveTrue();
    Page<ScraperConfigEntity> findByCompanyContainingIgnoreCase(String company, Pageable pageable);
    Optional<ScraperConfigEntity> findByCompany(String company);
    Page<ScraperConfigEntity> findByActive(Boolean active, Pageable pageable);

    Page<ScraperConfigEntity> findByCompanyContainingIgnoreCaseAndActive(
            String company,
            Boolean active,
            Pageable pageable
    );

    @Query("""
        SELECT c FROM ScraperConfigEntity c
        LEFT JOIN Job j ON j.company.name = c.company
        GROUP BY c
        ORDER BY COUNT(j) DESC
        """)
    Page<ScraperConfigEntity> findAllOrderByJobCountDesc(Pageable pageable);
}