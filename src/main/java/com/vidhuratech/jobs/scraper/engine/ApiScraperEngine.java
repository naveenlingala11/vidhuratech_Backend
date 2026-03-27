package com.vidhuratech.jobs.scraper.engine;

import com.vidhuratech.jobs.entity.Job;
import com.vidhuratech.jobs.scraper.providers.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiScraperEngine {

    private final GreenhouseScraper greenhouse;
    private final LeverScraper lever;
    private final WorkdayScraper workday;
    private final AshbyScraper ashby;
    private final SmartRecruitersScraper smart;
    private final LinkedInScraper linkedin;

    public ApiScraperEngine(
            GreenhouseScraper greenhouse,
            LeverScraper lever,
            WorkdayScraper workday,
            AshbyScraper ashby,
            SmartRecruitersScraper smart,
            LinkedInScraper linkedin
    ) {
        this.greenhouse = greenhouse;
        this.lever = lever;
        this.workday = workday;
        this.ashby = ashby;
        this.smart = smart;
        this.linkedin = linkedin;
    }

    public List<Job> run(ApiConfig cfg) {

        try {
            List<Job> jobs = switch (cfg.getType()) {
                case "greenhouse" -> greenhouse.scrape(cfg);
                case "lever" -> lever.scrape(cfg);
                case "workday" -> workday.scrape(cfg);
                case "ashby" -> ashby.scrape(cfg);
                case "smartrecruiters" -> smart.scrape(cfg);
                case "linkedin" -> linkedin.scrape(cfg);
                default -> List.of();
            };

            // ✅ fallback if empty
            if (jobs == null || jobs.isEmpty()) {
                return fallback(cfg);
            }

            return jobs;

        } catch (Exception e) {
            System.out.println("❌ Engine error: " + cfg.getCompany());
            return fallback(cfg);
        }
    }

    private List<Job> fallback(ApiConfig cfg) {
        try {
            System.out.println("🔁 Fallback → LinkedIn: " + cfg.getCompany());

            ApiConfig li = new ApiConfig();
            li.setCompany(cfg.getCompany());
            li.setType("linkedin");
            li.setUrl("https://www.linkedin.com/jobs/search?keywords="
                    + cfg.getCompany() + "&location=India");

            return linkedin.scrape(li);

        } catch (Exception e) {
            return List.of();
        }
    }
}