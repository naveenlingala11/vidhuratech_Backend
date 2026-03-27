package com.vidhuratech.jobs.scraper.engine;

import com.vidhuratech.jobs.entity.Job;
import java.util.List;

public interface ApiScraper {
    List<Job> scrape(ApiConfig config);
}