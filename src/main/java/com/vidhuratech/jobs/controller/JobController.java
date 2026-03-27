package com.vidhuratech.jobs.controller;

import com.vidhuratech.jobs.dto.FilterOption;
import com.vidhuratech.jobs.dto.JobResponse;
import com.vidhuratech.jobs.dto.PageResponse;
import com.vidhuratech.jobs.entity.Job;
import com.vidhuratech.jobs.service.JobService;
import com.vidhuratech.jobs.service.ScraperService;
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

    public JobController(JobService service, ScraperService scraperService) {
        this.service = service;
        this.scraperService = scraperService;
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
            @RequestParam(defaultValue = "latest") String sort,
            @PageableDefault(size = 15) Pageable pageable) {

        return service.searchAdvanced(
                keyword, locations, companies, skills,
                experience, remote, dateFilter, sort, pageable);
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
}