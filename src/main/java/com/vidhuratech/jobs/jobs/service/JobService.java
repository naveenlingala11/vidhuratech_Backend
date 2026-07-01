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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import org.jsoup.Jsoup;

@Service
public class JobService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepo;
    private final CompanyRepository companyRepo;
    private final SkillRepository skillRepo;
    private final TransactionTemplate transactionTemplate;

    public JobService(JobRepository jobRepo,
                      CompanyRepository companyRepo,
                      SkillRepository skillRepo,
                      PlatformTransactionManager transactionManager) {
        this.jobRepo = jobRepo;
        this.companyRepo = companyRepo;
        this.skillRepo = skillRepo;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    // ─────────────────────────────────────────────────────────
    // SAVE
    // ─────────────────────────────────────────────────────────
    public void saveJob(Job job, String companyName) {

        try {
            if (companyName == null || companyName.isBlank()) {
                return;
            }

            // 🔥 LOCATION CHECK FOR INDIA ONLY
            if (job.getLocation() == null || !isIndiaLocation(job.getLocation(), companyName)) {
                return;
            }

            // 🔥 VALIDATE APPLY LINK BEFORE SAVING
            if (job.getApplyLink() != null && !isUrlActive(job.getApplyLink())) {
                return;
            }

            // Normalise location to avoid duplicates in filters (e.g. bangalore vs Bengaluru)
            job.setLocation(normalizeLocation(job.getLocation()));

            transactionTemplate.executeWithoutResult(status -> {
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
                    return;
                }

                if (job.getPostedAt() == null) {
                    job.setPostedAt(LocalDateTime.now());
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

                saved.setSkills(skills);
                jobRepo.save(saved);
            });

            // 🔥 Detach entities to release JVM memory Heap buildup (Option B optimization)
            if (entityManager != null) {
                entityManager.clear();
            }

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 VERY IMPORTANT
        }
    }

    public static boolean isIndiaLocation(String location, String companyName) {
        if (location == null || location.isBlank()) {
            return false;
        }
        String loc = location.toLowerCase().trim();

        // Reject if location explicitly mentions foreign countries/cities
        List<String> foreignList = List.of(
            "united states", "usa", "uk", "united kingdom", "london", "singapore",
            "germany", "europe", "canada", "australia", "poland", "spain", "france",
            "brazil", "vietnam", "netherlands", "ireland", "tokyo", "dubai", "uae",
            "japan", "italy", "switzerland", "sweden", "austria", "belgium",
            "finland", "denmark", "norway", "san francisco", "new york", "seattle"
        );
        for (String foreign : foreignList) {
            if (loc.contains(foreign)) {
                return false;
            }
        }
        
        // Check "us" as separate token
        if (loc.equals("us") || loc.startsWith("us ") || loc.endsWith(" us") || 
            loc.contains(" us ") || loc.contains(",us") || loc.contains(", us") ||
            loc.contains("/us") || loc.contains("us/")) {
            return false;
        }

        // Accept if it explicitly contains "india"
        if (loc.contains("india")) {
            return true;
        }

        // Accept if it contains any major Indian tech hub cities
        List<String> indianCities = List.of(
            "bangalore", "bengaluru", "hyderabad", "pune", "mumbai", "noida", 
            "gurgaon", "gurugram", "delhi", "new delhi", "chennai", "kolkata", 
            "ahmedabad", "indore", "kochi", "cochin", "jaipur", "bhubaneswar", 
            "trivandrum", "thiruvananthapuram", "coimbatore", "chandigarh", "nagpur",
            "lucknow", "mysore", "mysuru", "visakhapatnam", "vizag", "patna",
            "guwahati", "bhopal", "ranchi", "raipur", "dehradun", "vadodara",
            "surat", "madurai", "trichy", "tiruchirappalli", "salem", "vijayawada",
            "guntur", "warangal", "hubli", "dharwad", "mangalore", "mangaluru",
            "calicut", "kozhikode", "jamshedpur"
        );
        for (String city : indianCities) {
            if (loc.contains(city)) {
                return true;
            }
        }

        // Accept remote ONLY if it's an Indian company or it explicitly says "India" (checked above)
        if (loc.contains("remote") || loc.contains("work from home")) {
            return isIndianCompany(companyName);
        }

        return false;
    }

    public static String normalizeLocation(String location) {
        if (location == null || location.isBlank()) {
            return "";
        }
        String loc = location.toLowerCase().trim();

        if (loc.contains("bangalore") || loc.contains("bengaluru") || loc.contains("bengalore")) {
            return "Bengaluru";
        }
        if (loc.contains("hyderabad")) {
            return "Hyderabad";
        }
        if (loc.contains("pune")) {
            return "Pune";
        }
        if (loc.contains("mumbai")) {
            return "Mumbai";
        }
        if (loc.contains("noida")) {
            return "Noida";
        }
        if (loc.contains("gurgaon") || loc.contains("gurugram")) {
            return "Gurugram";
        }
        if (loc.contains("delhi")) {
            return "New Delhi";
        }
        if (loc.contains("chennai")) {
            return "Chennai";
        }
        if (loc.contains("kolkata")) {
            return "Kolkata";
        }
        if (loc.contains("coimbatore")) {
            return "Coimbatore";
        }
        if (loc.contains("kochi") || loc.contains("cochin")) {
            return "Kochi";
        }
        if (loc.contains("trivandrum") || loc.contains("thiruvananthapuram")) {
            return "Trivandrum";
        }
        if (loc.contains("bhubaneswar")) {
            return "Bhubaneswar";
        }
        if (loc.contains("ahmedabad")) {
            return "Ahmedabad";
        }
        if (loc.contains("indore")) {
            return "Indore";
        }
        if (loc.contains("jaipur")) {
            return "Jaipur";
        }
        if (loc.contains("lucknow")) {
            return "Lucknow";
        }
        if (loc.contains("remote") || loc.contains("work from home")) {
            return "Remote";
        }
        
        // Capitalise words
        String[] words = location.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    private static boolean isIndianCompany(String companyName) {
        if (companyName == null) return false;
        String clean = companyName.toLowerCase().replace(" ", "").replace("-", "");
        Set<String> indianCompanies = Set.of(
            "groww", "phonepe", "razorpay", "cred", "meesho", "zeta", "pocketfm", 
            "wipro", "infosys", "hcl", "hcltech", "capgemini", "accenture", "deloitte", 
            "ey", "kpmg", "tcs", "paytm", "swiggy", "zomato", "delhivery", "inmobi", 
            "ola", "olaelectric", "atherenergy", "blinkit", "bigbasket", "nykaa", 
            "practo", "pharmeasy", "tata1mg", "apollo247", "upgrad", "unacademy", 
            "physicswallah", "scaler", "geeksforgeeks", "juspay", "postman", "zoho",
            "flipkart"
        );
        return indianCompanies.contains(clean);
    }

    @Transactional
    public void cleanNonIndiaJobs() {
        log.debug("🧹 STARTING CLEANUP OF NON-INDIA JOBS...");
        try {
            int skillsDeleted = jobRepo.deleteJobSkillsForNonIndiaJobs();
            log.debug("🔗 Removed {} job_skills references.", skillsDeleted);
            int deleted = jobRepo.deleteNonIndiaJobs();
            log.debug("🗑️ DELETED {} NON-INDIA JOBS (SQL-level cleanup).", deleted);
        } catch (Exception e) {
            log.error("❌ ERROR CLEANING NON-INDIA JOBS", e);
        }
    }

    @Transactional
    public void cleanExpiredJobs(String companyName, List<String> activeApplyLinks) {
        if (companyName == null || companyName.isBlank()) return;
        try {
            Company company = companyRepo.findByNameIgnoreCase(companyName).orElse(null);
            if (company == null) return;

            if (activeApplyLinks == null || activeApplyLinks.isEmpty()) {
                jobRepo.deleteByCompany(company);
            } else {
                jobRepo.deleteByCompanyAndApplyLinkNotIn(company, activeApplyLinks);
            }
        } catch (Exception e) {
            log.error("❌ ERROR CLEANING EXPIRED JOBS FOR {}", companyName, e);
        }
    }

    public static boolean isUrlActive(String url) {
        if (url == null || url.isBlank() || url.equals("#")) {
            return false;
        }
        try {
            org.jsoup.Connection.Response response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(6000)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .execute();
            
            int statusCode = response.statusCode();
            if (statusCode == 404) {
                return false;
            }
            
            String finalUrl = response.url().toString().toLowerCase();
            if (finalUrl.contains("/404") || finalUrl.contains("/expired") || finalUrl.contains("/notfound") || finalUrl.contains("/not-found")) {
                return false;
            }

            String contentType = response.contentType();
            if (contentType != null && contentType.toLowerCase().contains("text/html")) {
                String bodyText = response.body().toLowerCase();
                if (bodyText.contains("no longer available") || 
                    bodyText.contains("job is no longer active") || 
                    bodyText.contains("vacancy has been filled") || 
                    bodyText.contains("no longer accepting applications") ||
                    bodyText.contains("job posting has expired") ||
                    bodyText.contains("position is no longer open") ||
                    bodyText.contains("this job has expired") ||
                    bodyText.contains("opportunity is no longer available")) {
                    return false;
                }
            }
            return true;
        } catch (org.jsoup.HttpStatusException e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public void validateAllJobLinks() {
        log.info("🔍 STARTING MEMORY-OPTIMIZED BATCH VALIDATION OF JOB LINKS...");
        try {
            int pageSize = 100;
            int pageNumber = 0;
            long totalValidated = 0;
            long totalPruned = 0;

            // Bounded pool to restrict maximum concurrent HTTP request overhead to 5
            try (var executor = Executors.newFixedThreadPool(5)) {
                while (true) {
                    Page<Job> page = jobRepo.findAll(PageRequest.of(pageNumber, pageSize, Sort.by("id").ascending()));
                    List<Job> jobs = page.getContent();
                    if (jobs.isEmpty()) {
                        break;
                    }

                    List<Long> deadJobIds = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
                    List<java.util.concurrent.Future<?>> futures = new java.util.ArrayList<>();

                    for (Job job : jobs) {
                        futures.add(executor.submit(() -> {
                            String url = job.getApplyLink();
                            if (url != null && !url.contains("localhost") && !url.isBlank()) {
                                if (!isUrlActive(url)) {
                                    deadJobIds.add(job.getId());
                                    log.debug("🗑️ MARKED EXPIRED/404 JOB FOR DELETION: {} (ID: {})", job.getTitle(), job.getId());
                                }
                            }
                        }));
                    }

                    // Wait for all tasks in this batch to finish
                    for (java.util.concurrent.Future<?> f : futures) {
                        try {
                            f.get();
                        } catch (Exception ignored) {}
                    }

                    // Bulk delete dead jobs in a single database transaction
                    if (!deadJobIds.isEmpty()) {
                        transactionTemplate.executeWithoutResult(status -> {
                            jobRepo.deleteAllByIdInBatch(deadJobIds);
                        });
                        totalPruned += deadJobIds.size();
                    }

                    totalValidated += jobs.size();
                    log.info("🔄 Batch Validation Progress: Checked {} jobs, Pruned {} dead jobs.", totalValidated, totalPruned);

                    // Clear first-level cache (Hibernate session) to prevent Heap build-up
                    if (entityManager != null) {
                        entityManager.clear();
                    }

                    // If we deleted some jobs in this page, the database indices shift.
                    // To be safe, if we deleted records, we don't increment the page number
                    // so we scan the next set of records properly.
                    if (deadJobIds.isEmpty()) {
                        pageNumber++;
                    }
                }
            }
            log.info("✅ JOB LINKS VALIDATION COMPLETED. Checked: {}, Pruned: {}.", totalValidated, totalPruned);
        } catch (Exception e) {
            log.error("❌ ERROR VALIDATING JOB LINKS", e);
        }
    }

    // ─────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────
    @Transactional
    public Job addJob(Job job) {
        if (job.getCompany() == null) {
            throw new RuntimeException("Company is required.");
        }
        boolean exists = jobRepo.existsByTitleAndCompanyAndApplyLink(
                job.getTitle(),
                job.getCompany(),
                job.getApplyLink()
        );
        if (exists) {
            throw new RuntimeException("A job posting with the same title, company, and application link already exists.");
        }
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

    private Pageable ensureSorting(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "postedAt")
            );
        }
        return pageable;
    }

    // ─────────────────────────────────────────────────────────
    // QUERIES
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<JobResponse> getAllJobs(Pageable pageable) {
        return mapPage(jobRepo.findAll(ensureSorting(pageable)));
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> getByCategory(String category, Pageable pageable) {
        return mapPage(jobRepo.findByCategoryIgnoreCase(category, ensureSorting(pageable)));
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> getByLocation(String location, Pageable pageable) {
        return mapPage(jobRepo.findByLocationContainingIgnoreCase(location, ensureSorting(pageable)));
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> getByType(String type, Pageable pageable) {
        return mapPage(jobRepo.findByJobTypeIgnoreCase(type, ensureSorting(pageable)));
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> searchJobs(String keyword, Pageable pageable) {
        return mapPage(jobRepo.findByTitleContainingIgnoreCase(keyword, ensureSorting(pageable)));
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
            String jobType,
            String sort,
            Pageable pageable) {

        Specification<Job> spec = JobSpecification.filter(
                keyword, locations, companies, skills, experience, remote, dateFilter, jobType);

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