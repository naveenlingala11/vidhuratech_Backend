package com.vidhuratech.jobs.jobs.service;

import com.vidhuratech.jobs.jobs.entity.Job;
import com.vidhuratech.jobs.jobs.entity.ScraperConfigEntity;
import com.vidhuratech.jobs.jobs.repository.ScraperConfigRepository;
import com.vidhuratech.jobs.jobs.scraper.engine.ApiConfig;
import com.vidhuratech.jobs.jobs.scraper.engine.ApiScraperEngine;
import com.vidhuratech.jobs.jobs.scraper.engine.ScraperStatus;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ScraperService {

    private static final Logger log = LoggerFactory.getLogger(ScraperService.class);

    private final JobService jobService;
    private final ApiScraperEngine engine;
    private final ScraperConfigRepository repo;
    private final ScraperStatus status;

    public ScraperService(JobService jobService,
                          ApiScraperEngine engine,
                          ScraperConfigRepository repo,
                          ScraperStatus status) {
        this.jobService = jobService;
        this.engine = engine;
        this.repo = repo;
        this.status = status;
    }

    // ─────────────────────────────────────────
    // 🔥 MAIN SCRAPER (UNCHANGED + PARALLEL ADDED)
    // ─────────────────────────────────────────
    public void scrapeAll() {

        if (status.isRunning()) {
            log.info("Scraper already running, skipping.");
            return;
        }

        status.setRunning(true);

        try {
            log.info("SCRAPING STARTED");

            long globalStart = System.currentTimeMillis();

            List<ScraperConfigEntity> configs = repo.findByActiveTrue();

            if (configs.isEmpty()) {
                log.warn("No active companies found in DB");
                return;
            }

            // 🔥 OPTION 2: PARALLEL (FASTER)
            ExecutorService pool = Executors.newFixedThreadPool(3);

            for (ScraperConfigEntity entity : configs) {
                pool.submit(() -> {
                    processCompany(entity);
                    sleepRandom();
                });
            }

            pool.shutdown();
            try {
                if (!pool.awaitTermination(30, java.util.concurrent.TimeUnit.MINUTES)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException ex) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }

            long globalEnd = System.currentTimeMillis();

            log.info("SCRAPING COMPLETED in {} sec", (globalEnd - globalStart) / 1000);

            // Run sweep validation of all job links to prune dead ones
            jobService.validateAllJobLinks();

        } finally {
            status.setRunning(false);
            log.debug("Scraper status reset done");
        }
    }

    // ─────────────────────────────────────────
    // 🔥 COMPANY PROCESS (OLD LOGIC KEPT)
    // ─────────────────────────────────────────
    private void processCompany(ScraperConfigEntity entity) {

        long start = System.currentTimeMillis();

        String company = entity.getCompany();

        log.debug("Scraping: {}", company);

        ApiConfig cfg = new ApiConfig();
        cfg.setCompany(company);
        cfg.setType(entity.getType());
        cfg.setUrl(entity.getUrl());

        List<Job> jobs = null;
        boolean success = false;
        boolean scraperError = false;

        try {
            // 🔥 ADDED RETRY (3 TIMES)
            jobs = retry(cfg);

            if (jobs == null) {
                log.warn("Scraper failed (API/Network): {}", company);
                scraperError = true;
            } else {
                success = true;
                if (jobs.isEmpty()) {
                    log.debug("No jobs found: {}", company);
                }
            }

        } catch (Exception e) {
            log.warn("Scraper error [{}]: {}", company, e.getMessage());
            scraperError = true;
        }

        // ───── UPDATE SUCCESS / FAIL ─────
        if (success) {
            entity.setSuccessCount(entity.getSuccessCount() + 1);
            entity.setFailCount(0);
        } else if (scraperError) {
            entity.setFailCount(entity.getFailCount() + 1);
        }

        // 🔥 AUTO DISABLE BAD COMPANIES
        if (entity.getFailCount() >= 5) {
            entity.setActive(false);
            log.warn("Auto-disabled company (5+ failures): {}", company);
        }

        repo.save(entity);

        int scraped = jobs != null ? jobs.size() : 0;
        int saved = 0;

        if (jobs != null && !jobs.isEmpty()) {
            for (Job job : jobs) {
                try {
                    jobService.saveJob(job, company);
                    saved++;
                } catch (Exception e) {
                    log.debug("Save error [{}]", company);
                }
            }
        }

        if (jobs != null) {
            java.util.List<String> activeApplyLinks = new java.util.ArrayList<>();
            for (Job job : jobs) {
                if (job.getApplyLink() != null) {
                    activeApplyLinks.add(job.getApplyLink());
                }
            }
            jobService.cleanExpiredJobs(company, activeApplyLinks);
        }

        long end = System.currentTimeMillis();

        // Only log companies that actually scraped jobs (skip noise)
        if (scraped > 0) {
            log.info("[{}] Scraped: {}, Saved: {}, Time: {}s", company, scraped, saved, (end - start) / 1000);
        } else {
            log.debug("[{}] Scraped: 0, Time: {}s", company, (end - start) / 1000);
        }
    }

    // ─────────────────────────────────────────
    // 🔥 RETRY LOGIC (NEW ADD ONLY)
    // ─────────────────────────────────────────
    private List<Job> retry(ApiConfig cfg) {

        for (int i = 1; i <= 3; i++) {
            try {
                List<Job> jobs = engine.run(cfg);
                if (jobs != null) {
                    return jobs;
                }
            } catch (Exception e) {
                log.debug("Retry {} for {}", i, cfg.getCompany());
            }
        }

        return null;
    }

    // ─────────────────────────────────────────
    // 🔥 URL VALIDATOR (UNCHANGED)
    // ─────────────────────────────────────────
    public boolean isValidUrl(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .ignoreContentType(true)
                    .execute()
                    .statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private void sleepRandom() {
        try {
            Thread.sleep(1000 + new Random().nextInt(3000));
        } catch (Exception ignored) {}
    }
}