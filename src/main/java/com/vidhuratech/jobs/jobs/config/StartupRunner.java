package com.vidhuratech.jobs.jobs.config;

import com.vidhuratech.jobs.jobs.service.JobService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupRunner {

    @Bean
    public ApplicationRunner runAfterStartup(ScraperConfigLoader loader, JobService jobService) {
        return args -> {
            new Thread(() -> {
                try {
                    // ⏳ wait for app to be ready
                    Thread.sleep(20000);

                    loader.load(); // ✅ YOUR METHOD

                    // 🧹 Clean non-India legacy jobs from DB
                    jobService.cleanNonIndiaJobs();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        };
    }
}