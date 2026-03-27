package com.vidhuratech.jobs.scraper.providers;

import com.vidhuratech.jobs.entity.Job;
import com.vidhuratech.jobs.scraper.engine.ApiConfig;
import com.vidhuratech.jobs.scraper.engine.ApiScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class NaukriScraper implements ApiScraper {

    @Override
    public List<Job> scrape(ApiConfig config) {

        List<Job> jobs = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(config.getUrl())
                    .userAgent("Mozilla/5.0")
                    .timeout(15000)
                    .get();

            Elements cards = doc.select(".jobTuple");

            for (var c : cards) {
                try {
                    String title = c.select("a.title").text();
                    String location = c.select(".locWdth").text();
                    String link = c.select("a.title").attr("href");

                    if (title.isBlank()) continue;

                    jobs.add(build("Naukri", title, location, link));

                } catch (Exception ignored) {}
            }

        } catch (Exception e) {
            System.out.println("❌ Naukri blocked");
        }

        return jobs;
    }

    private Job build(String company, String title, String location, String link) {
        Job j = new Job();
        j.setTitle(title);
        j.setRole(title);
        j.setCompanyName(company);
        j.setLocation(location);
        j.setApplyLink(link);
        j.setSource("Naukri");
        j.setJobType("Experienced");
        j.setCategory("IT");
        j.setEmploymentType("Full-time");
        j.setSalary("Not Disclosed");
        j.setRemote(location.toLowerCase().contains("remote"));
        return j;
    }
}