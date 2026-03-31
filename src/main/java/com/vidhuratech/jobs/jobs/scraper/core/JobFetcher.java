package com.vidhuratech.jobs.jobs.scraper.core;

public interface JobFetcher {
    String fetch(String url) throws Exception;
}