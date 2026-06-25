package com.vidhuratech.jobs.jobs.repository;

import com.vidhuratech.jobs.jobs.entity.Company;
import com.vidhuratech.jobs.jobs.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
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

    void deleteByCompany(Company company);

    void deleteByCompanyAndApplyLinkNotIn(Company company, java.util.Collection<String> applyLinks);

    @Query("""
    SELECT COUNT(j)
    FROM Job j
    WHERE j.postedAt IS NOT NULL
    AND j.postedAt >= :date
    """)
    long countRecent(@Param("date") LocalDateTime date);

    @Modifying
    @Query(value = """
        DELETE FROM job_skills js
        WHERE js.job_id IN (
            SELECT j.id FROM jobs j
            WHERE j.location IS NULL
               OR (
                   LOWER(j.location) NOT LIKE '%india%'
                   AND LOWER(j.location) NOT LIKE '%bangalore%'
                   AND LOWER(j.location) NOT LIKE '%bengaluru%'
                   AND LOWER(j.location) NOT LIKE '%hyderabad%'
                   AND LOWER(j.location) NOT LIKE '%pune%'
                   AND LOWER(j.location) NOT LIKE '%mumbai%'
                   AND LOWER(j.location) NOT LIKE '%noida%'
                   AND LOWER(j.location) NOT LIKE '%gurgaon%'
                   AND LOWER(j.location) NOT LIKE '%gurugram%'
                   AND LOWER(j.location) NOT LIKE '%delhi%'
                   AND LOWER(j.location) NOT LIKE '%chennai%'
                   AND LOWER(j.location) NOT LIKE '%kolkata%'
                   AND LOWER(j.location) NOT LIKE '%ahmedabad%'
                   AND LOWER(j.location) NOT LIKE '%kochi%'
                   AND LOWER(j.location) NOT LIKE '%cochin%'
                   AND LOWER(j.location) NOT LIKE '%jaipur%'
                   AND LOWER(j.location) NOT LIKE '%indore%'
                   AND LOWER(j.location) NOT LIKE '%coimbatore%'
                   AND LOWER(j.location) NOT LIKE '%chandigarh%'
                   AND LOWER(j.location) NOT LIKE '%nagpur%'
                   AND LOWER(j.location) NOT LIKE '%lucknow%'
                   AND LOWER(j.location) NOT LIKE '%bhopal%'
                   AND LOWER(j.location) NOT LIKE '%trivandrum%'
                   AND LOWER(j.location) NOT LIKE '%thiruvananthapuram%'
                   AND LOWER(j.location) NOT LIKE '%visakhapatnam%'
                   AND LOWER(j.location) NOT LIKE '%vizag%'
                   AND LOWER(j.location) NOT LIKE '%mysore%'
                   AND LOWER(j.location) NOT LIKE '%mysuru%'
                   AND LOWER(j.location) NOT LIKE '%madurai%'
                   AND LOWER(j.location) NOT LIKE '%surat%'
                   AND LOWER(j.location) NOT LIKE '%vadodara%'
                   AND LOWER(j.location) NOT LIKE '%vijayawada%'
                   AND LOWER(j.location) NOT LIKE '%mangalore%'
                   AND LOWER(j.location) NOT LIKE '%mangaluru%'
                   AND LOWER(j.location) NOT LIKE '%remote%'
               )
        )
        """, nativeQuery = true)
    int deleteJobSkillsForNonIndiaJobs();

    @Modifying
    @Query(value = """
        DELETE FROM jobs j
        WHERE j.location IS NULL
           OR (
               LOWER(j.location) NOT LIKE '%india%'
               AND LOWER(j.location) NOT LIKE '%bangalore%'
               AND LOWER(j.location) NOT LIKE '%bengaluru%'
               AND LOWER(j.location) NOT LIKE '%hyderabad%'
               AND LOWER(j.location) NOT LIKE '%pune%'
               AND LOWER(j.location) NOT LIKE '%mumbai%'
               AND LOWER(j.location) NOT LIKE '%noida%'
               AND LOWER(j.location) NOT LIKE '%gurgaon%'
               AND LOWER(j.location) NOT LIKE '%gurugram%'
               AND LOWER(j.location) NOT LIKE '%delhi%'
               AND LOWER(j.location) NOT LIKE '%chennai%'
               AND LOWER(j.location) NOT LIKE '%kolkata%'
               AND LOWER(j.location) NOT LIKE '%ahmedabad%'
               AND LOWER(j.location) NOT LIKE '%kochi%'
               AND LOWER(j.location) NOT LIKE '%cochin%'
               AND LOWER(j.location) NOT LIKE '%jaipur%'
               AND LOWER(j.location) NOT LIKE '%indore%'
               AND LOWER(j.location) NOT LIKE '%coimbatore%'
               AND LOWER(j.location) NOT LIKE '%chandigarh%'
               AND LOWER(j.location) NOT LIKE '%nagpur%'
               AND LOWER(j.location) NOT LIKE '%lucknow%'
               AND LOWER(j.location) NOT LIKE '%bhopal%'
               AND LOWER(j.location) NOT LIKE '%trivandrum%'
               AND LOWER(j.location) NOT LIKE '%thiruvananthapuram%'
               AND LOWER(j.location) NOT LIKE '%visakhapatnam%'
               AND LOWER(j.location) NOT LIKE '%vizag%'
               AND LOWER(j.location) NOT LIKE '%mysore%'
               AND LOWER(j.location) NOT LIKE '%mysuru%'
               AND LOWER(j.location) NOT LIKE '%madurai%'
               AND LOWER(j.location) NOT LIKE '%surat%'
               AND LOWER(j.location) NOT LIKE '%vadodara%'
               AND LOWER(j.location) NOT LIKE '%vijayawada%'
               AND LOWER(j.location) NOT LIKE '%mangalore%'
               AND LOWER(j.location) NOT LIKE '%mangaluru%'
               AND LOWER(j.location) NOT LIKE '%remote%'
           )
        """, nativeQuery = true)
    int deleteNonIndiaJobs();
}