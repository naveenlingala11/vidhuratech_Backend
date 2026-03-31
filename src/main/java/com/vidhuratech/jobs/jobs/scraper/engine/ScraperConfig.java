package com.vidhuratech.jobs.jobs.scraper.engine;

import lombok.Data;

@Data
public class ScraperConfig {

    private String company;
    private String url;
    private String jobCardCss;
    private String titleCss;
    private String locationCss;
    private String linkCss;
    private String baseUrl;
    private String nextBtnCss;
}