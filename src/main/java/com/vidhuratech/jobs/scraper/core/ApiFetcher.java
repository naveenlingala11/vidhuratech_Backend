package com.vidhuratech.jobs.scraper.core;

import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.*;

@Component
public class ApiFetcher implements JobFetcher {

    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public String fetch(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }
}