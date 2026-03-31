package com.vidhuratech.jobs.jobs.scraper.engine;

import com.vidhuratech.jobs.jobs.entity.Job;
import com.vidhuratech.jobs.jobs.service.JobService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ScraperWorker {

    private final ScraperQueue     queue;
    private final ApiScraperEngine engine;
    private final JobService       jobService;

    private static final int THREADS = 6;

    public ScraperWorker(ScraperQueue queue,
                         ApiScraperEngine engine,
                         JobService jobService) {
        this.queue      = queue;
        this.engine     = engine;
        this.jobService = jobService;
    }

    @PostConstruct
    public void start() {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);

        for (int i = 0; i < THREADS; i++) {
            pool.submit(() -> {
                while (true) {
                    ApiConfig cfg = null;

                    try {
                        cfg = queue.take();

                        long start = System.currentTimeMillis();
                        System.out.println("🔍 START: " + cfg.getCompany());

                        List<Job> jobs = retry(cfg);

                        int saved = 0;

                        for (Job job : jobs) {
                            try {

                                // 🔥 INDIA + REMOTE FILTER
                                if (job.getLocation() != null) {
                                    String loc = job.getLocation().toLowerCase();

                                    if (!(loc.contains("india") || loc.contains("remote"))) {
                                        continue; // ❌ skip non-India jobs
                                    }
                                } else {
                                    continue; // ❌ skip null location
                                }

                                // ✅ SAVE
                                jobService.saveJob(job, job.getCompanyString());
                                saved++;

                            } catch (Exception e) {
                                System.out.println("⚠️ Save [" + cfg.getCompany() + "]: " + e.getMessage());
                            }
                        }

                        long end = System.currentTimeMillis();

                        System.out.printf(
                                "✅ DONE: %-25s scraped=%-4d saved=%-4d time=%ds%n",
                                cfg.getCompany(),
                                jobs.size(),
                                saved,
                                (end - start) / 1000
                        );

                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;

                    } catch (Exception e) {
                        System.out.println("❌ Worker [" +
                                (cfg != null ? cfg.getCompany() : "?") +
                                "]: " + e.getMessage());
                    }
                }
            });
        }
    }

    // ✅ FIXED METHOD
    private List<Job> retry(ApiConfig cfg) {

        int max = 3; // ✅ FIX: define max

        for (int i = 1; i <= max; i++) {
            try {
                return engine.run(cfg);

            } catch (Exception e) {
                System.out.printf(
                        "🔁 Retry %d/%d [%s] at %s%n",
                        i, max, cfg.getCompany(), new Date()
                );

                try {
                    Thread.sleep(2000L * i);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return List.of();
    }
}