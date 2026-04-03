package com.vidhuratech.jobs.jobs.service;

import com.vidhuratech.jobs.jobs.dto.FilterOption;
import com.vidhuratech.jobs.jobs.dto.JobResponse;
import com.vidhuratech.jobs.jobs.dto.PageResponse;
import com.vidhuratech.jobs.jobs.entity.Company;
import com.vidhuratech.jobs.jobs.entity.Job;
import com.vidhuratech.jobs.jobs.entity.Skill;
import com.vidhuratech.jobs.jobs.repository.CompanyRepository;
import com.vidhuratech.jobs.jobs.repository.JobRepository;
import com.vidhuratech.jobs.jobs.repository.SkillRepository;
import com.vidhuratech.jobs.jobs.spec.JobSpecification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class JobService {

    private final JobRepository jobRepo;
    private final CompanyRepository companyRepo;
    private final SkillRepository skillRepo;

    public JobService(JobRepository jobRepo,
                      CompanyRepository companyRepo,
                      SkillRepository skillRepo) {
        this.jobRepo = jobRepo;
        this.companyRepo = companyRepo;
        this.skillRepo = skillRepo;
    }

    // ─────────────────────────────────────────────────────────
    // SAVE
    // ─────────────────────────────────────────────────────────
    @Transactional
    public void saveJob(Job job, String companyName) {

        try {
            if (companyName == null || companyName.isBlank()) {
                System.out.println("❌ SKIPPED (NO COMPANY): " + job.getTitle());
                return;
            }

            Company company = companyRepo.findByNameIgnoreCase(companyName)
                    .orElseGet(() -> {
                        Company c = new Company();
                        c.setName(companyName);
                        return companyRepo.save(c);
                    });

            job.setCompany(company);

            // 🔥 DUPLICATE CHECK (MOST IMPORTANT)
            boolean exists = jobRepo.existsByTitleAndCompanyAndApplyLink(
                    job.getTitle(),
                    company,
                    job.getApplyLink()
            );

            if (exists) {
                System.out.println("⚠️ DUPLICATE SKIPPED: " + job.getTitle());
                return;
            }

            Job saved = jobRepo.save(job);
            jobRepo.flush();

            Set<Skill> skills = new HashSet<>();

            if (job.getSkillsCsv() != null) {
                for (String s : job.getSkillsCsv().split(",")) {
                    String skill = s.trim();
                    if (!skill.isEmpty()) {
                        skills.add(
                                skillRepo.findByNameIgnoreCase(skill)
                                        .orElseGet(() -> skillRepo.save(new Skill(null, skill)))
                        );
                    }
                }
            }

            if (job.getPostedAt() == null) {
                job.setPostedAt(LocalDateTime.now());
            }

            saved.setSkills(skills);
            jobRepo.save(saved);

            System.out.println("✅ SAVED: " + job.getTitle());

        } catch (Exception e) {
            System.out.println("❌ FAILED: " + job.getTitle());
            e.printStackTrace(); // 🔥 VERY IMPORTANT
        }
    }

    // ─────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────
    @Transactional
    public Job addJob(Job job) {
        job.setPostedAt(LocalDateTime.now());
        return jobRepo.save(job);
    }

    @Transactional(readOnly = true)
    public Optional<JobResponse> getById(Long id) {
        return jobRepo.findById(id)
                .map(job -> {
                    if (job.getCompany() != null) {
                        job.getCompany().getName(); // 🔥 force load
                    }
                    return new JobResponse(job);
                });
    }

    // ─────────────────────────────────────────────────────────
    // COMMON METHOD (🔥 avoids duplication)
    // ─────────────────────────────────────────────────────────
    private PageResponse<JobResponse> mapPage(Page<Job> page) {

        List<JobResponse> content = page.getContent()
                .stream()
                .map(job -> {
                    // 🔥 FORCE LOAD COMPANY (fix lazy issue)
                    if (job.getCompany() != null) {
                        job.getCompany().getName();
                    }
                    return new JobResponse(job);
                })
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }

    // ─────────────────────────────────────────────────────────
    // QUERIES
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<JobResponse> getAllJobs(Pageable pageable) {
        return mapPage(jobRepo.findAll(pageable));
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> getByCategory(String category, Pageable pageable) {
        return mapPage(jobRepo.findByCategoryIgnoreCase(category, pageable));
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> getByLocation(String location, Pageable pageable) {
        return mapPage(jobRepo.findByLocationContainingIgnoreCase(location, pageable));
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> getByType(String type, Pageable pageable) {
        return mapPage(jobRepo.findByJobTypeIgnoreCase(type, pageable));
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> searchJobs(String keyword, Pageable pageable) {
        return mapPage(jobRepo.findByTitleContainingIgnoreCase(keyword, pageable));
    }

    // ─────────────────────────────────────────────────────────
    // ADVANCED SEARCH
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<JobResponse> searchAdvanced(
            String keyword,
            List<String> locations,
            List<String> companies,
            List<String> skills,
            String experience,
            Boolean remote,
            String dateFilter,
            String sort,
            Pageable pageable) {

        Specification<Job> spec = JobSpecification.filter(
                keyword, locations, companies, skills, experience, remote, dateFilter);

        Sort sorting = switch (sort == null ? "latest" : sort) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "postedAt");
            case "title" -> Sort.by(Sort.Direction.ASC, "title");
            case "company" -> Sort.by(Sort.Direction.ASC, "company.name");
            default -> Sort.by(Sort.Direction.DESC, "postedAt");
        };

        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sorting
        );

        return mapPage(jobRepo.findAll(spec, sorted));
    }

    // ─────────────────────────────────────────────────────────
    // FILTERS
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<FilterOption> getCompanyFilters() {
        return jobRepo.getCompanyCounts().stream()
                .limit(50)
                .map(r -> new FilterOption((String) r[0], (long) r[1]))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FilterOption> getLocationFilters() {
        return jobRepo.getLocationCounts().stream()
                .limit(30)
                .map(r -> new FilterOption((String) r[0], (long) r[1]))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FilterOption> getSkillCounts() {
        return jobRepo.getSkillCounts().stream()
                .limit(40)
                .map(r -> new FilterOption((String) r[0], (long) r[1]))
                .toList();
    }
}