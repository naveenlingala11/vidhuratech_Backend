package com.vidhuratech.jobs.jobs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vidhuratech.jobs.jobs.entity.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdzunaScraperService {

    private final JobService jobService;
    private final JobEnrichmentService enrichmentService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${adzuna.app-id}")
    private String appId;

    @Value("${adzuna.app-key}")
    private String appKey;

    public AdzunaScraperService(JobService jobService, JobEnrichmentService enrichmentService) {
        this.jobService = jobService;
        this.enrichmentService = enrichmentService;
    }

    public boolean isConfigured() {
        return !("dummy_id".equals(appId) || "dummy_key".equals(appKey) || appId == null || appKey == null || appId.isBlank() || appKey.isBlank());
    }

    public String getAppId() {
        return appId;
    }

    public Map<String, Object> scrapeAdzunaJobs(String searchTerm, int pageLimit) {
        int savedCount = 0;
        int failedCount = 0;
        List<String> logs = new ArrayList<>();

        if ("dummy_id".equals(appId) || "dummy_key".equals(appKey) || appId == null || appKey == null || appId.isBlank() || appKey.isBlank()) {
            logs.add("⚠️ Adzuna API credentials are not configured. Skipping scrape.");
            return Map.of("success", false, "message", "Credentials missing", "logs", logs);
        }

        logs.add("🚀 Starting Adzuna scrape for: " + searchTerm);

        try {
            for (int page = 1; page <= pageLimit; page++) {
                // Build the URL for Adzuna India
                String url = String.format(
                        "https://api.adzuna.com/v1/api/jobs/in/search/%d?app_id=%s&app_key=%s&what=%s&results_per_page=50&content-type=application/json",
                        page,
                        appId,
                        appKey,
                        java.net.URLEncoder.encode(searchTerm, java.nio.charset.StandardCharsets.UTF_8)
                );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "Mozilla/5.0")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    logs.add("❌ Adzuna API returned status code " + response.statusCode() + " on page " + page);
                    break;
                }

                JsonNode root = mapper.readTree(response.body());
                JsonNode results = root.path("results");

                if (!results.isArray() || results.isEmpty()) {
                    logs.add("ℹ️ No more results found on page " + page);
                    break;
                }

                for (JsonNode node : results) {
                    try {
                        String title = node.path("title").asText("");
                        String description = node.path("description").asText("");
                        String companyName = node.path("company").path("display_name").asText("");
                        String location = node.path("location").path("display_name").asText("India");
                        String redirectUrl = node.path("redirect_url").asText("");
                        String createdStr = node.path("created").asText("");

                        if (title.isBlank() || redirectUrl.isBlank() || companyName.isBlank()) {
                            continue;
                        }

                        // Clean title from HTML tags if any
                        title = title.replaceAll("<[^>]*>", "");
                        description = description.replaceAll("<[^>]*>", "");

                        Job job = new Job();
                        job.setTitle(title);
                        job.setRole(title);
                        job.setLocation(location);
                        job.setApplyLink(redirectUrl);
                        job.setDescription("<p>" + description + "</p>");
                        job.setSource("Adzuna");
                        job.setJobType("Experienced");
                        job.setEmploymentType("Full-time");
                        job.setSalary("Not Disclosed");

                        // Parse date
                        if (!createdStr.isBlank()) {
                            try {
                                // Adzuna date format is usually ISO 8601, e.g. "2026-06-24T12:00:00Z"
                                job.setPostedAt(LocalDateTime.parse(createdStr, DateTimeFormatter.ISO_DATE_TIME));
                            } catch (Exception ignored) {
                                job.setPostedAt(LocalDateTime.now());
                            }
                        } else {
                            job.setPostedAt(LocalDateTime.now());
                        }

                        // Enrich
                        job.setCategory(enrichmentService.detectCategory(title));
                        List<String> skills = enrichmentService.extractSkills(description);
                        job.setSkillsCsv(String.join(",", skills));

                        // Save using standardized jobService
                        jobService.saveJob(job, companyName);
                        savedCount++;

                    } catch (Exception e) {
                        failedCount++;
                    }
                }
                
                logs.add("✅ Scraped page " + page + " successfully.");
                // Sleep briefly to be respectful to the API rate limit
                Thread.sleep(500);
            }

            logs.add("🏁 Adzuna scrape finished. Saved " + savedCount + " jobs.");
            return Map.of(
                    "success", true,
                    "jobsSavedCount", savedCount,
                    "failedCount", failedCount,
                    "message", "Successfully scraped " + savedCount + " jobs from Adzuna.",
                    "logs", logs
            );

        } catch (Exception e) {
            logs.add("❌ Error during scrape: " + e.getMessage());
            return Map.of("success", false, "message", e.getMessage(), "logs", logs);
        }
    }
}
