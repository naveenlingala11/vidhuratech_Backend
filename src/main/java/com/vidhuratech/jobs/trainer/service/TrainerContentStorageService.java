package com.vidhuratech.jobs.trainer.service;

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
import java.util.UUID;

@Service
public class TrainerContentStorageService {

    private static final long MAX_SIZE = 15 * 1024 * 1024; // 15MB
    private static final String FOLDER = "trainer-contents";

    private final S3Client s3Client;
    private final String bucket;
    private final String publicUrl;

    public TrainerContentStorageService(
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
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
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
                    e.getMessage() == null ? "Trainer file upload failed" : e.getMessage()
            );
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty or missing");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("File size exceeds the limit of 15MB");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new RuntimeException("Invalid file name");
        }
    }

    private String sanitizeFileName(String fileName) {
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        String safeName = normalized
                .replaceAll("[^a-zA-Z0-9._-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("(^-|-$)", "");

        return safeName.isBlank() ? "file" : safeName;
    }

    private String stripTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
