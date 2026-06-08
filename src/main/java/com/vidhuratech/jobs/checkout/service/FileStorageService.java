package com.vidhuratech.jobs.checkout.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.text.Normalizer;
import java.util.*;

@Service
public class FileStorageService {

    private static final long MAX_SIZE = 5 * 1024 * 1024;

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "application/pdf", ".pdf",
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".pdf", ".jpg", ".jpeg", ".png", ".webp");

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        validate(file);

        try {
            String contentType = normalizeContentType(file.getContentType());
            String safeOriginalName = sanitizeFileName(file.getOriginalFilename());
            String extension = extensionFor(contentType, safeOriginalName);
            String fileName = UUID.randomUUID() + "_" + stripExtension(safeOriginalName) + extension;

            Path uploadPath = uploadRoot.resolve("payment-proofs").normalize();
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName).normalize();

            if (!filePath.startsWith(uploadPath)) {
                throw new RuntimeException("Invalid upload path");
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/payment-proofs/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Secure file upload failed");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Upload file is required");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("Upload file must be below 5MB");
        }

        String contentType = normalizeContentType(file.getContentType());

        if (!ALLOWED_TYPES.containsKey(contentType)) {
            throw new RuntimeException("Only PDF, JPG, PNG, and WEBP files are allowed");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new RuntimeException("Invalid upload file name");
        }

        String lowerName = originalName.toLowerCase(Locale.ROOT);

        boolean validExtension = ALLOWED_EXTENSIONS.stream().anyMatch(lowerName::endsWith);

        if (!validExtension) {
            throw new RuntimeException("Invalid upload file extension");
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) return "";
        if (contentType.equalsIgnoreCase("image/jpg")) return "image/jpeg";
        return contentType.toLowerCase(Locale.ROOT);
    }

    private String sanitizeFileName(String fileName) {
        String normalized = Normalizer.normalize(String.valueOf(fileName), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        String safeName = normalized
                .replaceAll("[^a-zA-Z0-9._-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("(^-|-$)", "");

        return safeName.isBlank() ? "upload" : safeName;
    }

    private String extensionFor(String contentType, String fileName) {
        if (contentType.equals("image/jpeg")
                && fileName.toLowerCase(Locale.ROOT).endsWith(".jpeg")) {
            return ".jpeg";
        }

        return ALLOWED_TYPES.get(contentType);
    }

    private String stripExtension(String fileName) {
        return fileName.replaceFirst("\\.[^.]+$", "");
    }
}