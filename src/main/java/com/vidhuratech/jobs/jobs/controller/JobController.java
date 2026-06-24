package com.vidhuratech.jobs.jobs.controller;

import com.vidhuratech.jobs.jobs.dto.FilterOption;
import com.vidhuratech.jobs.jobs.dto.JobResponse;
import com.vidhuratech.jobs.jobs.dto.PageResponse;
import com.vidhuratech.jobs.jobs.entity.Job;
import com.vidhuratech.jobs.jobs.service.JobSeederService;
import com.vidhuratech.jobs.jobs.service.JobService;
import com.vidhuratech.jobs.jobs.service.ScraperService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/jobs")
@CrossOrigin("*")
public class JobController {

    private final JobService service;
    private final ScraperService scraperService;
    private final JobSeederService seederService;
    private final com.vidhuratech.jobs.jobs.service.AdzunaScraperService adzunaService;

    public JobController(JobService service,
                         ScraperService scraperService,
                         JobSeederService seederService,
                         com.vidhuratech.jobs.jobs.service.AdzunaScraperService adzunaService) {
        this.service = service;
        this.scraperService = scraperService;
        this.seederService = seederService;
        this.adzunaService = adzunaService;
    }

    // ✅ FIX: Missing @GetMapping
    @GetMapping
    public PageResponse<JobResponse> getJobs(
            @PageableDefault(size = 15) Pageable pageable) {
        return service.getAllJobs(pageable);
    }

    // ✅ FIX: Return DTO
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ FIX: convert to DTO
    @GetMapping("/category")
    public PageResponse<JobResponse> byCategory(
            @RequestParam String category,
            @PageableDefault(size = 15) Pageable pageable) {
        return service.getByCategory(category, pageable);
    }

    @GetMapping("/location")
    public PageResponse<JobResponse> byLocation(
            @RequestParam String location,
            @PageableDefault(size = 15) Pageable pageable) {
        return service.getByLocation(location, pageable);
    }

    @GetMapping("/type")
    public PageResponse<JobResponse> byType(
            @RequestParam String type,
            @PageableDefault(size = 15) Pageable pageable) {
        return service.getByType(type, pageable);
    }

    @GetMapping("/search")
    public PageResponse<JobResponse> search(
            @RequestParam String keyword,
            @PageableDefault(size = 15) Pageable pageable) {
        return service.searchJobs(keyword, pageable);
    }

    @GetMapping("/advanced")
    public PageResponse<JobResponse> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> locations,
            @RequestParam(required = false) List<String> companies,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) Boolean remote,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = false) String jobType,
            @RequestParam(defaultValue = "latest") String sort,
            @PageableDefault(size = 15) Pageable pageable) {

        return service.searchAdvanced(
                keyword, locations, companies, skills,
                experience, remote, dateFilter, jobType, sort, pageable);
    }

    @GetMapping("/filters")
    public Map<String, List<FilterOption>> getFilters() {
        Map<String, List<FilterOption>> map = new HashMap<>();
        map.put("companies", service.getCompanyFilters());
        map.put("locations", service.getLocationFilters());
        map.put("skills", service.getSkillCounts());
        return map;
    }

    // ⚠️ Keep as entity (admin internal)
    @PostMapping("/admin/add")
    public Job addJob(@RequestBody Job job) {
        return service.addJob(job);
    }

    @GetMapping("/scrape")
    public String triggerScrape() {
        new Thread(scraperService::scrapeAll).start();
        return "🚀 Scraping started! Check server logs.";
    }

    @GetMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedJobs(
            @RequestParam(defaultValue = "1000000") int count,
            @RequestParam(defaultValue = "true") boolean clean) {
        Map<String, Object> result = seederService.seedJobs(count, clean);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/seed/progress")
    public ResponseEntity<Map<String, Object>> getSeedProgress() {
        Map<String, Object> progress = new HashMap<>();
        progress.put("isSeeding", JobSeederService.isSeeding);
        progress.put("currentProgress", JobSeederService.currentSeedProgress);
        return ResponseEntity.ok(progress);
    }

    @DeleteMapping("/seed/clean")
    public ResponseEntity<Map<String, Object>> cleanSeededJobs() {
        Map<String, Object> result = seederService.cleanSeededJobs();
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/scrape/adzuna")
    public ResponseEntity<Map<String, Object>> scrapeAdzuna(
            @RequestParam(defaultValue = "Software Developer") String what,
            @RequestParam(defaultValue = "3") int pages) {
        Map<String, Object> result = adzunaService.scrapeAdzunaJobs(what, pages);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/scrape/adzuna/status")
    public ResponseEntity<Map<String, Object>> getAdzunaStatus() {
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("configured", adzunaService.isConfigured());
        status.put("appId", adzunaService.getAppId() != null && !adzunaService.getAppId().equals("dummy_id") ? adzunaService.getAppId() : null);
        return ResponseEntity.ok(status);
    }
}