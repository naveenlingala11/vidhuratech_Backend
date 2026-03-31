package com.vidhuratech.jobs.jobs.service;

import com.vidhuratech.jobs.jobs.entity.Job;
import com.vidhuratech.jobs.jobs.entity.ScraperConfigEntity;
import com.vidhuratech.jobs.jobs.repository.ScraperConfigRepository;
import com.vidhuratech.jobs.jobs.scraper.engine.ApiConfig;
import com.vidhuratech.jobs.jobs.scraper.engine.ApiScraperEngine;
import com.vidhuratech.jobs.jobs.scraper.engine.ScraperStatus;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ScraperService {

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
            System.out.println("⛔ Scraper already running...");
            return;
        }

        status.setRunning(true);

        try {
            System.out.println("\n🚀 SCRAPING STARTED\n");

            long globalStart = System.currentTimeMillis();

            List<ScraperConfigEntity> configs = repo.findByActiveTrue();

            if (configs.isEmpty()) {
                System.out.println("⚠️ No active companies found in DB");
                return;
            }

            // 🔥 OPTION 1: NORMAL (SAFE)
            // for (ScraperConfigEntity entity : configs) {
            //     processCompany(entity);
            // }

            // 🔥 OPTION 2: PARALLEL (FASTER)
            ExecutorService pool = Executors.newFixedThreadPool(3); // NOT 6

            for (ScraperConfigEntity entity : configs) {
                pool.submit(() -> {
                    processCompany(entity);
                    sleepRandom();
                });
            }
            long globalEnd = System.currentTimeMillis();

            System.out.println("🏁 SCRAPING COMPLETED");
            System.out.println("⏱ TOTAL TIME: " + ((globalEnd - globalStart) / 1000) + " sec");

        } finally {
            status.setRunning(false); // 🔥 VERY IMPORTANT
            System.out.println("🔁 STATUS RESET DONE\n");
        }
    }

    // ─────────────────────────────────────────
    // 🔥 COMPANY PROCESS (OLD LOGIC KEPT)
    // ─────────────────────────────────────────
    private void processCompany(ScraperConfigEntity entity) {

        long start = System.currentTimeMillis();

        String company = entity.getCompany();

        System.out.println("🔍 START: " + company);

        ApiConfig cfg = new ApiConfig();
        cfg.setCompany(company);
        cfg.setType(entity.getType());
        cfg.setUrl(entity.getUrl());

        List<Job> jobs = List.of();
        boolean success = false;

        try {
            // 🔥 ADDED RETRY (3 TIMES)
            jobs = retry(cfg);

            if (jobs == null || jobs.isEmpty()) {
                System.out.println("⚠️ NO JOBS FOUND: " + company);
                success = false;
            } else {
                success = true;
            }

        } catch (Exception e) {
            System.out.println("❌ ERROR [" + company + "]: " + e.getMessage());
            success = false;
        }

        // ───── UPDATE SUCCESS / FAIL ─────
        if (success) {
            entity.setSuccessCount(entity.getSuccessCount() + 1);
        } else {
            entity.setFailCount(entity.getFailCount() + 1);
        }

        // 🔥 AUTO DISABLE BAD COMPANIES
        if (entity.getFailCount() >= 5) {
            entity.setActive(false);
            System.out.println("🚫 AUTO DISABLED: " + company);
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
                    System.out.println("⚠️ SAVE ERROR [" + company + "]");
                }
            }
        }

        long end = System.currentTimeMillis();

        // 🔥 SAME LOG FORMAT (UNCHANGED)
        System.out.println("\n==============================");
        System.out.println("🏢 COMPANY: " + company);
        System.out.println("📥 Scraped: " + scraped);
        System.out.println("💾 Saved:   " + saved);
        System.out.println("⏱ Time:     " + ((end - start) / 1000) + " sec");
        System.out.println("==============================\n");
    }

    // ─────────────────────────────────────────
    // 🔥 RETRY LOGIC (NEW ADD ONLY)
    // ─────────────────────────────────────────
    private List<Job> retry(ApiConfig cfg) {

        for (int i = 1; i <= 3; i++) {
            try {
                return engine.run(cfg);
            } catch (Exception e) {
                System.out.println("🔁 Retry " + i + " → " + cfg.getCompany());
            }
        }

        return List.of();
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