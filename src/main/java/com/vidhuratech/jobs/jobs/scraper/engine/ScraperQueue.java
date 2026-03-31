package com.vidhuratech.jobs.jobs.scraper.engine;

import org.springframework.stereotype.Component;
import java.util.concurrent.*;

@Component
public class ScraperQueue {

    private final BlockingQueue<ApiConfig> queue = new LinkedBlockingQueue<>();

    public void add(ApiConfig config) {
        queue.add(config);
    }

    public ApiConfig take() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty(); // assuming BlockingQueue inside
    }
}