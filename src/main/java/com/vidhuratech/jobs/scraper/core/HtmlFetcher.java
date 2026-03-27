package com.vidhuratech.jobs.scraper.core;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Component
public class HtmlFetcher implements JobFetcher {

    @Override
    public String fetch(String url) throws Exception {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get()
                .html();
    }
}