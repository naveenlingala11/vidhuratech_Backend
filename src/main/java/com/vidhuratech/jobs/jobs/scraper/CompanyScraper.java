package com.vidhuratech.jobs.jobs.scraper;

import com.vidhuratech.jobs.jobs.entity.Job;
import java.util.List;

public interface CompanyScraper {
    List<Job> scrapeJobs() throws InterruptedException;
    String getCompanyName();
}