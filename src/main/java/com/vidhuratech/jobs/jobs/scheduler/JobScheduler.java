package com.vidhuratech.jobs.jobs.scheduler;

import com.vidhuratech.jobs.jobs.service.ScraperService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class JobScheduler {

    private final ScraperService scraperService;

    // AtomicBoolean for thread-safe guard (replaces static boolean)
    private final AtomicBoolean running = new AtomicBoolean(false);

    public JobScheduler(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    /** Daily scrape at 8:00 AM */
    @Scheduled(cron = "${scraper.cron.daily}")
    public void runDailyScrape() {
        triggerScrape("DAILY");
    }

    /** Periodic scrape every 6 hours */
    @Scheduled(cron = "${scraper.cron.periodic}")
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