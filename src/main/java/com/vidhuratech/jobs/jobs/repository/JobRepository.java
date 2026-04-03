package com.vidhuratech.jobs.jobs.repository;

import com.vidhuratech.jobs.jobs.entity.Company;
import com.vidhuratech.jobs.jobs.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    // ── Duplicate check ───────────────────────────────────────────────────────
    Optional<Job> findByTitleAndCompany_IdAndApplyLink(String title, Long companyId, String applyLink);

    // ── Paged queries ─────────────────────────────────────────────────────────
    Page<Job> findByLocationContainingIgnoreCase(String location, Pageable pageable);
    Page<Job> findByJobTypeIgnoreCase(String jobType, Pageable pageable);
    Page<Job> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Job> findByCategoryIgnoreCase(String category, Pageable pageable);

    // ── Filter counts ─────────────────────────────────────────────────────────
    @Query("SELECT j.company.name, COUNT(j) FROM Job j WHERE j.company IS NOT NULL " +
            "GROUP BY j.company.name ORDER BY COUNT(j) DESC")
    List<Object[]> getCompanyCounts();

    @Query("SELECT j.location, COUNT(j) FROM Job j WHERE j.location IS NOT NULL " +
            "GROUP BY j.location ORDER BY COUNT(j) DESC")
    List<Object[]> getLocationCounts();

    @Query("SELECT s.name, COUNT(j) FROM Job j JOIN j.skills s " +
            "GROUP BY s.name ORDER BY COUNT(j) DESC")
    List<Object[]> getSkillCounts();

    boolean existsByTitleAndCompanyAndApplyLink(String title, Company company, String applyLink);

    @Query("""
    SELECT COUNT(j)
    FROM Job j
    WHERE j.postedAt IS NOT NULL
    AND j.postedAt >= :date
    """)
    long countRecent(@Param("date") LocalDateTime date);}