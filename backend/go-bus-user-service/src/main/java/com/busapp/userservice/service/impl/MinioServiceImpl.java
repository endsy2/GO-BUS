package com.busapp.userservice.service.impl;

import com.busapp.userservice.exception.FileStorageException;
import com.busapp.userservice.service.MinioService;
import com.busapp.userservice.util.MinioUtil;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final MinioUtil minioUtil;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    @Override
    public String uploadProfileImage(MultipartFile file, Long userId) {
        try {
            // Validate file
            validateFile(file);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String objectName = String.format("profiles/%d/%s%s", 
                    userId, UUID.randomUUID(), extension);

            // Upload to MinIO
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            log.info("Profile image uploaded successfully: {}", objectName);
            
            // Return the object name (we'll generate presigned URLs on demand)
            return objectName;

        } catch (Exception e) {
            log.error("Failed to upload profile image for user {}", userId, e);
            throw new FileStorageException("Failed to upload profile image: " + e.getMessage());
        }
    }

    @Override
    public void deleteProfileImage(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return;
        }

        try {
            // Extract object name if full URL is provided
            String actualObjectName = minioUtil.extractObjectName(objectName);
            
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(actualObjectName)
                            .build()
            );
            
            log.info("Profile image deleted successfully: {}", actualObjectName);
        } catch (Exception e) {
            log.error("Failed to delete profile image: {}", objectName, e);
            // Don't throw exception, just log the error
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException("File size exceeds maximum limit of 5MB");
        }

        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : ALLOWED_CONTENT_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            throw new FileStorageException("Invalid file type. Only images are allowed (JPEG, PNG, GIF, WebP)");
        }
    }


}
