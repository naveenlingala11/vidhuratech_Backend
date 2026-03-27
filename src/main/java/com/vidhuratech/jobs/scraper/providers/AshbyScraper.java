package com.vidhuratech.jobs.scraper.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vidhuratech.jobs.entity.Job;
import com.vidhuratech.jobs.scraper.engine.ApiConfig;
import com.vidhuratech.jobs.scraper.engine.ApiScraper;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Ashby ATS  →  https://jobs.ashbyhq.com/api/non-user-graphql?op=ApiJobBoardWithTeams
 * POST with JSON body: {"operationName":"ApiJobBoardWithTeams","variables":{"organizationHostedJobsPageName":"<slug>"},"query":"..."}
 * Simpler: use their public REST endpoint: https://api.ashbyhq.com/posting-api/job-board/<slug>?includeCompensation=false
 */
@Component
public class AshbyScraper implements ApiScraper {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Job> scrape(ApiConfig config) {
        List<Job> jobs = new ArrayList<>();
        try {
            String json = Jsoup.connect(config.getUrl())
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0")
                    .timeout(15000)
                    .get().body().text();

            JsonNode root = mapper.readTree(json);
            JsonNode arr  = root.path("jobs");
            if (arr.isMissingNode() || !arr.isArray()) return jobs;

            for (JsonNode j : arr) {
                try {
                    String title    = j.path("title").asText("");
                    String location = j.path("location").asText("India");
                    String link     = j.path("jobUrl").asText("");

                    if (title.isBlank() || link.isBlank()) continue;
                    jobs.add(build(config.getCompany(), title, location, link));
                } catch (Exception ignored) {}
            }

            System.out.println("✅ Ashby [" + config.getCompany() + "] → " + jobs.size());
        } catch (Exception e) {
            System.out.println("❌ Ashby [" + config.getCompany() + "]: " + e.getMessage());
        }
        return jobs;
    }

    private Job build(String company, String title, String location, String link) {
        Job job = new Job();
        job.setTitle(title);
        job.setRole(title);
        job.setCompanyName(company);
        job.setLocation(location);
        job.setApplyLink(link);
        job.setSource(company);
        job.setJobType("Experienced");
        job.setCategory("IT");
        job.setEmploymentType("Full-time");
        job.setSalary("Not Disclosed");
        job.setRemote(location.toLowerCase().contains("remote"));
        return job;
    }
}