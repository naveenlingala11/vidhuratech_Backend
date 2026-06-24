package com.vidhuratech.jobs.jobs.scheduler;

import com.vidhuratech.jobs.jobs.service.ScraperService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class JobScheduler {

    private final ScraperService scraperService;
    private final com.vidhuratech.jobs.jobs.service.AdzunaScraperService adzunaService;

    @Value("${adzuna.default-pages:2}")
    private int defaultPages;

    @Value("${adzuna.crawl-roles:Software Developer,Java Developer,Frontend Developer,Python Developer,React Developer,Full Stack Developer,DevOps Engineer,Data Analyst,QA Engineer,Android Developer}")
    private List<String> crawlRoles;

    // AtomicBoolean for thread-safe guard (replaces static boolean)
    private final AtomicBoolean running = new AtomicBoolean(false);

    public JobScheduler(ScraperService scraperService,
            com.vidhuratech.jobs.jobs.service.AdzunaScraperService adzunaService) {
        this.scraperService = scraperService;
        this.adzunaService = adzunaService;
    }

    /** Periodic scrape runs 3 times daily (9:00 AM, 3:00 PM, 9:00 PM Asia/Kolkata) */
    @Scheduled(cron = "${scraper.cron.periodic:0 0 9,15,21 * * *}", zone = "Asia/Kolkata")
    public void runPeriodicScrape() {
        triggerScrape("PERIODIC");
    }

    private void triggerScrape(String label) {
        if (!running.compareAndSet(false, true)) {
            System.out.println("⛔ " + label + " scrape SKIPPED — already running");
            return;
        }

        new Thread(() -> {
            long start = System.currentTimeMillis();
            try {
                System.out.println("🚀 " + label + " scrape STARTED  @ " + LocalDateTime.now());
                scraperService.scrapeAll();

                System.out.println("🚀 " + label + " Adzuna API scrape STARTED for roles: " + crawlRoles);
                for (String role : crawlRoles) {
                    try {
                        System.out.println("🔍 Scraping Adzuna for role: " + role + " (" + defaultPages + " pages)...");
                        adzunaService.scrapeAdzunaJobs(role, defaultPages);
                        // Sleep briefly between roles to respect API rate limits
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        System.err.println("❌ Error scraping role " + role + ": " + e.getMessage());
                    }
                }

                long elapsed = (System.currentTimeMillis() - start) / 1000;
                System.out.println("✅ " + label + " scrape DONE     @ " + LocalDateTime.now()
                        + "  (" + elapsed + "s)");
            } catch (Exception e) {
                System.err.println("❌ " + label + " scrape ERROR: " + e.getMessage());
            } finally {
                running.set(false);
            }
        }, label + "-scraper").start();
    }
}