package com.vidhuratech.jobs.jobs.config;

import com.vidhuratech.jobs.jobs.service.JobService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner {

    private final ScraperConfigLoader loader;
    private final JobService jobService;

    public StartupRunner(ScraperConfigLoader loader, JobService jobService) {
        this.loader = loader;
        this.jobService = jobService;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        try {
            // ⏳ Small delay to let all beans fully initialize
            Thread.sleep(5000);

            // ✅ Each call is @Transactional — gets its own DB connection and releases immediately
            loader.load();

            // 🧹 Clean non-India legacy jobs (SQL-level, fast)
            jobService.cleanNonIndiaJobs();

        } catch (Exception e) {
            System.err.println("❌ Startup task error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}