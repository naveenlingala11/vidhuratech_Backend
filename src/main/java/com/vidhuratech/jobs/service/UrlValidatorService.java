package com.vidhuratech.jobs.service;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Service
public class UrlValidatorService {

    public boolean isValidUrl(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .ignoreContentType(true)
                    .execute()
                    .statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}