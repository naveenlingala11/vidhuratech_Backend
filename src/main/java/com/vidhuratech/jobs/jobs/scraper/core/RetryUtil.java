package com.vidhuratech.jobs.jobs.scraper.core;

import java.util.function.Supplier;

public class RetryUtil {

    public static <T> T retry(Supplier<T> fn, int attempts) {

        for (int i = 0; i < attempts; i++) {
            try {
                return fn.get();
            } catch (Exception e) {
                try { Thread.sleep(2000); } catch (Exception ignored) {}
            }
        }

        throw new RuntimeException("Failed after retries");
    }
}