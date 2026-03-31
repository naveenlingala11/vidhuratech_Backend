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

/**
 * SmartRecruiters public API:
 * GET https://api.smartrecruiters.com/v1/companies/{companyId}/postings
 */
@Component
public class SmartRecruitersScraper implements ApiScraper {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Job> scrape(ApiConfig config) {
        List<Job> jobs = new ArrayList<>();
        int offset = 0;
        int limit  = 100;

        while (true) {
            try {
                String url = config.getUrl() + "?limit=" + limit + "&offset=" + offset;

                String json = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .userAgent("Mozilla/5.0")
                        .timeout(15000)
                        .get().body().text();

                JsonNode root = mapper.readTree(json);
                JsonNode arr  = root.path("content");

                if (arr.isMissingNode() || !arr.isArray() || arr.isEmpty()) break;

                for (JsonNode j : arr) {
                    try {
                        String title    = j.path("name").asText("");
                        String location = j.path("location").path("city").asText("India");
                        String id       = j.path("id").asText("");
                        String link     = "https://careers.smartrecruiters.com/" +
                                config.getCompany().replaceAll("\\s+", "") + "/" + id;

                        if (title.isBlank() || id.isBlank()) continue;
                        jobs.add(build(config.getCompany(), title, location, link));
                    } catch (Exception ignored) {}
                }

                long total = root.path("totalFound").asLong(0);
                offset += limit;
                if (offset >= total) break;

            } catch (Exception e) {
                System.out.println("❌ SmartRecruiters [" + config.getCompany() + "]: " + e.getMessage());
                break;
            }
        }

        System.out.println("✅ SmartRecruiters [" + config.getCompany() + "] → " + jobs.size());
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