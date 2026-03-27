package com.vidhuratech.jobs.scraper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vidhuratech.jobs.entity.Job;

import java.net.URI;
import java.net.http.*;
import java.time.LocalDateTime;
import java.util.*;

public abstract class BaseApiScraper {

    protected final HttpClient client = HttpClient.newHttpClient();
    protected final ObjectMapper mapper = new ObjectMapper();

    protected JsonNode get(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return mapper.readTree(res.body());
        } catch (Exception e) {
            System.out.println("API Error: " + e.getMessage());
            return null;
        }
    }

    private String extractSkills(String title) {
        List<String> skills = new ArrayList<>();

        title = title.toLowerCase();

        if (title.contains("java")) skills.add("Java");
        if (title.contains("react")) skills.add("React");
        if (title.contains("angular")) skills.add("Angular");
        if (title.contains("python")) skills.add("Python");
        if (title.contains("spring")) skills.add("Spring Boot");
        if (title.contains("aws")) skills.add("AWS");
        if (title.contains("node")) skills.add("Node.js");

        if (skills.isEmpty()) skills.add("General IT");

        return String.join(",", skills); // 🔥 IMPORTANT
    }

    protected Job build(String title, String company, String location, String exp, String link) {
        Job job = new Job();

        job.setTitle(title);
        job.setRole(title); // 🔥 useful for filtering
        job.setCompanyName(company);
        job.setLocation(location == null ? "India" : location);
        job.setExperience(exp == null ? "0-15+ years" : exp);
        job.setApplyLink(link);

        job.setPostedAt(LocalDateTime.now());

        job.setJobType("Experienced");
        job.setCategory("IT");
        job.setEmploymentType("Full-time");
        job.setRemote(location != null && location.toLowerCase().contains("remote"));

        // 🔥 SMART SKILLS
        job.setSkillsCsv("java,react");
        return job;
    }
}