package com.vidhuratech.jobs.checkout.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    public String store(MultipartFile file) {

        try {

            String fileName =
                    UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path uploadPath =
                    Paths.get("uploads/payment-proofs");

            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "/uploads/payment-proofs/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("File upload failed");
        }
    }
}