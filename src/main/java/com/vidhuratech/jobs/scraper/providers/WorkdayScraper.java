package com.vidhuratech.jobs.scraper.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vidhuratech.jobs.entity.Job;
import com.vidhuratech.jobs.scraper.engine.ApiConfig;
import com.vidhuratech.jobs.scraper.engine.ApiScraper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WorkdayScraper implements ApiScraper {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Job> scrape(ApiConfig config) {
        List<Job> jobs = new ArrayList<>();
        int offset = 0;
        int limit  = 20;

        while (true) {
            try {
                String body = String.format(
                        "{\"limit\":%d,\"offset\":%d,\"searchText\":\"\",\"locations\":[],\"jobFamilies\":[]}",
                        limit, offset);

                String json = Jsoup.connect(config.getUrl())
                        .ignoreContentType(true)
                        .method(Connection.Method.POST)
                        .requestBody(body)
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "Mozilla/5.0")
                        .timeout(20000)
                        .execute().body();

                JsonNode root = mapper.readTree(json);
                JsonNode arr  = root.path("jobPostings");

                if (arr.isMissingNode() || arr.isEmpty()) break;

                for (JsonNode j : arr) {
                    try {
                        String title    = j.path("title").asText("");
                        String location = j.path("locationsText").asText("India");
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
                System.out.println("❌ Workday [" + config.getCompany() + "] offset=" + offset + ": " + e.getMessage());
                break;
            }
        }

        System.out.println("✅ Workday [" + config.getCompany() + "] → " + jobs.size());
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