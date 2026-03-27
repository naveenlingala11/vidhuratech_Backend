package com.vidhuratech.jobs.scraper.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vidhuratech.jobs.entity.Job;
import com.vidhuratech.jobs.scraper.engine.ApiConfig;
import com.vidhuratech.jobs.scraper.engine.ApiScraper;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GreenhouseScraper implements ApiScraper {

    private final ObjectMapper mapper = new ObjectMapper();

    // ✅ FIXED SLUGS (only override where needed)
    private static final Map<String, String> FIX = Map.of(
            "Notion", "notionlabs",
            "Datadog", "datadoghq",
            "1Password", "agilebits"
    );

    @Override
    public List<Job> scrape(ApiConfig config) {

        List<Job> jobs = new ArrayList<>();

        try {

            String url = config.getUrl();

            if (FIX.containsKey(config.getCompany())) {
                String slug = FIX.get(config.getCompany());
                url = "https://boards-api.greenhouse.io/v1/boards/" + slug + "/jobs?content=true";
            }

            String json = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0")
                    .timeout(20000)
                    .execute()
                    .body();

            JsonNode root = mapper.readTree(json);
            JsonNode arr = root.path("jobs");

            if (!arr.isArray()) return jobs;

            for (JsonNode j : arr) {
                try {
                    String title = j.path("title").asText("");
                    String link = j.path("absolute_url").asText("");

                    if (title.isBlank()) continue;

                    jobs.add(build(config.getCompany(), title, link));

                } catch (Exception ignored) {}
            }

        } catch (Exception e) {
            System.out.println("❌ Greenhouse failed: " + config.getCompany());
        }

        return jobs;
    }

    private Job build(String company, String title, String link) {
        Job j = new Job();
        j.setTitle(title);
        j.setRole(title);
        j.setCompanyName(company);
        j.setApplyLink(link);
        j.setSource("Greenhouse");
        j.setLocation("India");
        j.setJobType("Experienced");
        j.setCategory("IT");
        j.setEmploymentType("Full-time");
        j.setSalary("Not Disclosed");
        j.setRemote(false);
        return j;
    }
}