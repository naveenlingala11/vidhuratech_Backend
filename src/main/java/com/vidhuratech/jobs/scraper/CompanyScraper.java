package com.vidhuratech.jobs.scraper;

import com.vidhuratech.jobs.entity.Job;
import java.util.List;

public interface CompanyScraper {
    List<Job> scrapeJobs() throws InterruptedException;
    String getCompanyName();
}