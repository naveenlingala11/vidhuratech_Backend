package com.vidhuratech.jobs.jobs.scraper.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vidhuratech.jobs.jobs.entity.Job;
import com.vidhuratech.jobs.jobs.scraper.engine.ApiConfig;
import com.vidhuratech.jobs.jobs.scraper.engine.ApiScraper;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LeverScraper implements ApiScraper {

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

            JsonNode arr = mapper.readTree(json);
            if (!arr.isArray()) return jobs;

            for (JsonNode j : arr) {
                try {
                    String title    = j.path("text").asText("");
                    String location = j.path("categories").path("location").asText("India");
                    String link     = j.path("hostedUrl").asText("");

                    if (title.isBlank() || link.isBlank()) continue;

                    jobs.add(build(config.getCompany(), title, location, link));
                } catch (Exception ignored) {}
            }

            System.out.println("✅ Lever [" + config.getCompany() + "] → " + jobs.size());
        } catch (Exception e) {
            System.out.println("❌ Lever [" + config.getCompany() + "]: " + e.getMessage());
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