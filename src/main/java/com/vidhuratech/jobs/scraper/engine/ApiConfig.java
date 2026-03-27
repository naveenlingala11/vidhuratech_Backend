package com.vidhuratech.jobs.scraper.engine;

import lombok.Data;

@Data
public class ApiConfig {
    private String company;
    private String type;
    private String url;
}