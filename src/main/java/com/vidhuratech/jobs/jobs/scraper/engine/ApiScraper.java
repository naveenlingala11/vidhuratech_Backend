package com.vidhuratech.jobs.jobs.scraper.engine;

import com.vidhuratech.jobs.jobs.entity.Job;
import java.util.List;

public interface ApiScraper {
    List<Job> scrape(ApiConfig config);
}