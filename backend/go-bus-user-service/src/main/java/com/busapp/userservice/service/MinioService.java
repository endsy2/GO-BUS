package com.busapp.userservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {
    
    /**
     * Upload profile image to MinIO
     * @param file The image file to upload
     * @param userId The user ID for organizing files
     * @return The URL to access the uploaded file
     */
    String uploadProfileImage(MultipartFile file, Long userId);
    
    /**
     * Delete profile image from MinIO
     * @param imageUrl The URL of the image to delete
     */
    void deleteProfileImage(String imageUrl);
    
    /**
     * Get presigned URL for accessing private images
     * @param objectName The object name in MinIO
     * @return Presigned URL valid for limited time
     */
}
