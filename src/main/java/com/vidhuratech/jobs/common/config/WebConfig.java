package com.vidhuratech.jobs.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    /*
     * Do not expose the whole uploads directory.
     * StaticResourceConfig only exposes explicitly public course thumbnails.
     */
}