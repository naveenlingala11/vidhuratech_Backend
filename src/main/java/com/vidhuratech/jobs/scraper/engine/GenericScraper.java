package com.vidhuratech.jobs.scraper.engine;

import com.vidhuratech.jobs.entity.Job;
import com.vidhuratech.jobs.scraper.BaseSeleniumScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GenericScraper extends BaseSeleniumScraper {

    public List<Job> scrape(ScraperConfig config) {
        // 1️⃣ Fast path: Jsoup (no browser needed)
        try {
            List<Job> jobs = scrapeWithJsoup(config);
            if (!jobs.isEmpty()) return jobs;
        } catch (Exception e) {
            System.out.println("⚠️ Jsoup failed for [" + config.getCompany() + "] → falling back to Selenium");
        }

        // 2️⃣ Slow path: Selenium
        return scrapeWithSelenium(config);
    }

    // ── Jsoup path ────────────────────────────────────────────────────────────
    private List<Job> scrapeWithJsoup(ScraperConfig config) throws Exception {
        Document doc = Jsoup.connect(config.getUrl())
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(12000)
                .get();

        Elements cards = doc.select(config.getJobCardCss());
        List<Job> jobs = new ArrayList<>();

        for (var c : cards) {
            try {
                String title    = c.select(config.getTitleCss()).text().trim();
                String location = c.select(config.getLocationCss()).text().trim();
                String link     = c.select(config.getLinkCss()).attr("href");

                if (!link.startsWith("http"))
                    link = config.getBaseUrl() + link;

                if (title.isBlank()) continue;

                jobs.add(build(title, config.getCompany(), location,
                        "0-10 years", "", "Experienced",
                        extractSkills(title), link));
            } catch (Exception ignored) {}
        }
        return jobs;
    }

    // ── Selenium path ─────────────────────────────────────────────────────────
    private List<Job> scrapeWithSelenium(ScraperConfig config) {
        var driver = createDriver();
        try {
            driver.get(config.getUrl());

            return paginateAndScrape(
                    driver,
                    config.getJobCardCss(),
                    card -> {
                        String title    = text(card, config.getTitleCss());
                        String location = text(card, config.getLocationCss());
                        String link     = attr(card, config.getLinkCss(), "href");

                        if (!link.startsWith("http"))
                            link = config.getBaseUrl() + link;

                        if (title.isBlank()) return null;

                        return build(title, config.getCompany(), location,
                                "0-10 years", "", "Experienced",
                                extractSkills(title), link);
                    },
                    config.getNextBtnCss()
            );
        } finally {
            driver.quit();
        }
    }
}