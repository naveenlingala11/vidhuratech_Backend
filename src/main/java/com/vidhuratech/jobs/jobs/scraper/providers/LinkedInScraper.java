//

package com.vidhuratech.jobs.jobs.scraper.providers;

import com.vidhuratech.jobs.jobs.entity.Job;
import com.vidhuratech.jobs.jobs.scraper.engine.ApiConfig;
import com.vidhuratech.jobs.jobs.scraper.engine.ApiScraper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LinkedInScraper implements ApiScraper {

    @Override
    public List<Job> scrape(ApiConfig config) {

        // 🔴 KEEP SAFE (no 403 crashes)
        System.out.println("⚠️ LinkedIn skipped: " + config.getCompany());
        return List.of();
    }
}