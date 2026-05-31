package com.vidhuratech.jobs.lms.course.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Set;

@Service
public class CourseThumbnailStorageService {

    private static final long MAX_SIZE = 800 * 1024;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/jpg"
    );

    public String store(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("Thumbnail image is required");
            }

            if (file.getSize() > MAX_SIZE) {
                throw new RuntimeException("Thumbnail image must be below 800KB");
            }

            String contentType = file.getContentType();

            if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
                throw new RuntimeException("Only JPG, PNG, and WEBP images are allowed");
            }

            String base64 = Base64.getEncoder().encodeToString(file.getBytes());

            return "data:" + contentType + ";base64," + base64;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() == null ? "Thumbnail upload failed" : e.getMessage());
        }
    }
}