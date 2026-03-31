package com.vidhuratech.jobs.jobs.scraper.engine;

import org.springframework.stereotype.Component;

@Component
public class ScraperStatus {

    private volatile boolean running = false;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
