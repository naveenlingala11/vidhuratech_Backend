package com.vidhuratech.jobs.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String thumbnailPath = Path.of(uploadDir, "course-thumbnails")
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        registry.addResourceHandler("/course-thumbnails/**")
                .addResourceLocations(thumbnailPath);

        // Old DB/frontend URLs support. Later remove after cleanup.
        registry.addResourceHandler("/uploads/course-thumbnails/**")
                .addResourceLocations(thumbnailPath);
    }
}