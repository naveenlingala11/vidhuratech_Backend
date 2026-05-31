package com.vidhuratech.jobs.lms.course.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class CourseThumbnailStorageService {

    private static final long MAX_SIZE = 4 * 1024 * 1024;
    private static final String FOLDER = "course-thumbnails";

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final S3Client s3Client;
    private final String bucket;
    private final String publicUrl;

    public CourseThumbnailStorageService(
            @Value("${do.spaces.key}") String accessKey,
            @Value("${do.spaces.secret}") String secretKey,
            @Value("${do.spaces.region}") String region,
            @Value("${do.spaces.bucket}") String bucket,
            @Value("${do.spaces.endpoint}") String endpoint,
            @Value("${do.spaces.public-url}") String publicUrl
    ) {
        this.bucket = bucket;
        this.publicUrl = stripTrailingSlash(publicUrl);

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }

    public String store(MultipartFile file) {
        validate(file);

        try {
            String contentType = normalizeContentType(file.getContentType());
            String objectKey = FOLDER + "/" + UUID.randomUUID() + "-" +
                    sanitizeFileName(file.getOriginalFilename());

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return publicUrl + "/" + objectKey;

        } catch (Exception e) {
            throw new RuntimeException(
                    e.getMessage() == null ? "Thumbnail upload failed" : e.getMessage()
            );
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Thumbnail image is required");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("Thumbnail image must be below 4MB");
        }

        String contentType = normalizeContentType(file.getContentType());

        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new RuntimeException("Only JPG, PNG, and WEBP images are allowed");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new RuntimeException("Invalid thumbnail file name");
        }

        String lowerName = originalName.toLowerCase(Locale.ROOT);

        boolean validExtension = lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".png")
                || lowerName.endsWith(".webp");

        if (!validExtension) {
            throw new RuntimeException("Thumbnail must be JPG, PNG, or WEBP");
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }

        if (contentType.equalsIgnoreCase("image/jpg")) {
            return "image/jpeg";
        }

        return contentType.toLowerCase(Locale.ROOT);
    }

    private String sanitizeFileName(String fileName) {
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        String safeName = normalized
                .replaceAll("[^a-zA-Z0-9._-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("(^-|-$)", "");

        return safeName.isBlank() ? "thumbnail.jpg" : safeName;
    }

    private String stripTrailingSlash(String value) {
        if (value == null) {
            return "";
        }

        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}