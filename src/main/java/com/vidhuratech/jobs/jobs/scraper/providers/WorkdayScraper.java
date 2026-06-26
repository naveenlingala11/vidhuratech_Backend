package com.vidhuratech.jobs.jobs.scraper.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vidhuratech.jobs.jobs.entity.Job;
import com.vidhuratech.jobs.jobs.scraper.engine.ApiConfig;
import com.vidhuratech.jobs.jobs.scraper.engine.ApiScraper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WorkdayScraper implements ApiScraper {

    private static final Logger log = LoggerFactory.getLogger(WorkdayScraper.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Job> scrape(ApiConfig config) {
        List<Job> jobs = new ArrayList<>();
        int offset = 0;
        int limit  = 20;

        boolean failed = false;
        while (true) {
            try {
                String body = String.format(
                        "{\"appliedFacets\":{},\"limit\":%d,\"offset\":%d,\"searchText\":\"\"}",
                        limit, offset);

                String json = Jsoup.connect(config.getUrl())
                        .ignoreContentType(true)
                        .method(Connection.Method.POST)
                        .requestBody(body)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(20000)
                        .execute().body();

                JsonNode root = mapper.readTree(json);
                JsonNode arr  = root.path("jobPostings");

                if (arr.isMissingNode() || arr.isEmpty()) break;

                for (JsonNode j : arr) {
                    try {
                        String title    = j.path("title").asText("");
                        String location = j.path("locationsText").asText("");
                        String path     = j.path("externalPath").asText("");
                        // derive base from config url: https://TENANT.wd1.myworkdayjobs.com/wday/cxs/...
                        String base = config.getUrl().replaceAll("/wday/cxs/.*", "");
                        String link = path.isBlank() ? config.getUrl() : base + path;

                        if (title.isBlank()) continue;
                        jobs.add(build(config.getCompany(), title, location, link));
                    } catch (Exception ignored) {}
                }

                if (arr.size() < limit) break;
                offset += limit;

            } catch (Exception e) {
                log.debug("❌ Workday [" + config.getCompany() + "] offset=" + offset + ": " + e.getMessage());
                failed = true;
                break;
            }
        }

        if (failed && jobs.isEmpty()) {
            return null;
        }

        log.debug("✅ Workday [" + config.getCompany() + "] → " + jobs.size());
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