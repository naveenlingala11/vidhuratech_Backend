package com.vidhuratech.jobs.jobs.controller;

import com.vidhuratech.jobs.jobs.entity.ScraperConfigEntity;
import com.vidhuratech.jobs.jobs.repository.JobRepository;
import com.vidhuratech.jobs.jobs.repository.ScraperConfigRepository;
import com.vidhuratech.jobs.jobs.scraper.engine.ScraperStatus;
import com.vidhuratech.jobs.jobs.service.ScraperService;
import com.vidhuratech.jobs.jobs.service.UrlValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin("*")
public class AdminController {

    @Autowired
    private ScraperConfigRepository repo;

    @Autowired
    private JobRepository jobRepo;

    @Autowired
    private ScraperStatus status;

    @Autowired
    private ScraperService  scraperService;

    @Autowired
    private UrlValidatorService urlValidator;

    // ✅ ONLY ONE ENDPOINT (FIXED)
    @GetMapping("/companies")
    public Page<ScraperConfigEntity> getCompanies(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "company") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {

        // 🔥 HANDLE JOB SORT SEPARATELY
        if (sortBy.equalsIgnoreCase("jobs")) {
            return repo.findAllOrderByJobCountDesc(pageable);
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        if (active != null && search.isBlank()) {
            return repo.findByActive(active, sorted);
        }

        if (active != null) {
            return repo.findByCompanyContainingIgnoreCaseAndActive(search, active, sorted);
        }

        if (search.isBlank()) {
            return repo.findAll(sorted);
        }

        return repo.findByCompanyContainingIgnoreCase(search, sorted);
    }

    @DeleteMapping("/companies/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }

    @PutMapping("/companies/{id}/toggle")
    public ScraperConfigEntity toggle(@PathVariable Long id) {
        ScraperConfigEntity c = repo.findById(id).orElseThrow();
        c.setActive(!c.isActive());
        return repo.save(c);
    }

    @GetMapping("/analytics")
    public Map<String, Object> analytics() {
        Map<String, Object> map = new HashMap<>();

        map.put("totalJobs", jobRepo.count());
        map.put("today", jobRepo.countRecent(LocalDateTime.now().minusDays(1)));
        map.put("week", jobRepo.countRecent(LocalDateTime.now().minusDays(7)));
        map.put("month", jobRepo.countRecent(LocalDateTime.now().minusDays(30)));

        return map;
    }

    @GetMapping("/scrape/status")
    public Map<String, Boolean> getStatus() {
        return Map.of("running", status.isRunning());
    }

    @GetMapping("/scrape")
    public Map<String, String> triggerScrape() {
        new Thread(scraperService::scrapeAll).start();
        return Map.of("message", "Scraping started");
    }

    @PostMapping("/companies")
    public ScraperConfigEntity add(@RequestBody ScraperConfigEntity c) {

        if (!urlValidator.isValidUrl(c.getUrl())) {
            throw new RuntimeException("❌ Invalid URL");
        }

        return repo.save(c);
    }
}