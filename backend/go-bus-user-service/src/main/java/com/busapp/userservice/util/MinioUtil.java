package com.busapp.userservice.util;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MinioUtil {
    @Value("${minio.bucket-name}")
    private String bucketName;
    private final MinioClient minioClient;

    // Use the public-endpoint client so presigned URLs are signed for the host
    // the browser can actually reach (see MinioConfig#minioPresignClient).
    public MinioUtil(@Qualifier("minioPresignClient") MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Cacheable(value = "presignedUrls", key = "#objectName", unless = "#result == null")
    public String getPresignedUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }

        try {
            String actualObjectName = extractObjectName(objectName);

            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(actualObjectName)
                            .expiry(7, TimeUnit.DAYS) // URL valid for 7 days
                            .build()
            );

            log.debug("Generated presigned URL for: {}", actualObjectName);
            return url;
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", objectName, e);
            return null;
        }
    }

    /**
     * Async version with timeout for non-blocking presigned URL generation
     */
    @Async("minioTaskExecutor")
    public CompletableFuture<String> getPresignedUrlAsync(String objectName) {
        return CompletableFuture.supplyAsync(() -> getPresignedUrl(objectName));
    }

    /**
     * Get presigned URL with timeout fallback
     * Returns null if MinIO call takes longer than specified timeout
     */
    public String getPresignedUrlWithTimeout(String objectName, long timeoutMs) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }

        try {
            return getPresignedUrlAsync(objectName)
                    .completeOnTimeout(null, timeoutMs, TimeUnit.MILLISECONDS)
                    .get();
        } catch (Exception e) {
            log.warn("Timeout or error getting presigned URL for: {}", objectName, e);
            return null;
        }
    }
    public String extractObjectName(String input) {
        // If it's already just an object name, return it
        if (!input.startsWith("http")) {
            return input;
        }

        // Extract object name from URL
        // Format: http://minio:9000/bucket-name/object-name
        try {
            String[] parts = input.split("/" + bucketName + "/");
            if (parts.length > 1) {
                return parts[1].split("\\?")[0]; // Remove query parameters if any
            }
        } catch (Exception e) {
            log.warn("Failed to extract object name from URL: {}", input);
        }

        return input;
    }
}
