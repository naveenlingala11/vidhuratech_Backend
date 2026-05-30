package com.vidhuratech.jobs.lms.course.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class CourseThumbnailStorageService {

    private static final long MAX_SIZE = 4 * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/jpg"
    );

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public String store(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("Thumbnail image is required");
            }

            if (file.getSize() > MAX_SIZE) {
                throw new RuntimeException("Thumbnail image must be below 4MB");
            }

            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
                throw new RuntimeException("Only JPG, PNG, and WEBP images are allowed");
            }

            String originalName = file.getOriginalFilename() == null
                    ? "course-thumbnail"
                    : file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-_]", "_");

            String fileName = UUID.randomUUID() + "_" + originalName;

            Path uploadPath = Paths.get(uploadDir, "course-thumbnails")
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName).normalize();

            if (!filePath.startsWith(uploadPath)) {
                throw new RuntimeException("Invalid file name");
            }

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "/course-thumbnails/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() == null ? "Thumbnail upload failed" : e.getMessage());
        }
    }
}