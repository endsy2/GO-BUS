package com.busapp.userservice.controller;

import com.busapp.userservice.dto.response.ApiResponse;
import com.busapp.userservice.exception.ResourceNotFoundException;
import com.busapp.userservice.model.User;
import com.busapp.userservice.repository.UserRepository;
import com.busapp.userservice.service.MinioService;
import com.busapp.userservice.service.UserService;
import com.busapp.userservice.util.MinioUtil;
import com.busapp.userservice.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final MinioService minioService;
    private final UserService userService;
    private final MinioUtil minioUtil;
    private final UserRepository userRepository;
    private final UserUtil userUtil;

    /**
     * Upload or update profile image
     * @param file The image file to upload
     * @return Response with image URL
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfileImage(
            @RequestParam("file") MultipartFile file) {
        
        try {
            // Now this works because HeaderAuthenticationFilter populates SecurityContext
            Long userId = userUtil.getCurrentUserId();
            
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.of(
                                HttpStatus.UNAUTHORIZED.value(),
                                "User not authenticated",
                                null
                        ));
            }
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            // Delete old image if exists
            if (user.getImage() != null) {
                minioService.deleteProfileImage(user.getImage());
            }
            
            // Upload to MinIO
            String objectName = minioService.uploadProfileImage(file, userId);
            
            // Update user profile with new image reference
            userService.updateProfileImage(userId, objectName);
            
            // Generate presigned URL for immediate access. This can return null if
            // signing fails (e.g. MinIO public endpoint unreachable); the image is
            // already uploaded and saved, so don't fail the request — the client can
            // re-fetch the URL via GET /profile/image/url. Use a null-tolerant map
            // (Map.of throws NPE on null values).
            String imageUrl = minioUtil.getPresignedUrl(objectName);

            Map<String, String> payload = new HashMap<>();
            payload.put("objectName", objectName);
            payload.put("imageUrl", imageUrl);

            return ResponseEntity.ok(
                    ApiResponse.of(
                            HttpStatus.OK.value(),
                            "Profile image uploaded successfully",
                            payload
                    )
            );
        } catch (Exception e) {
            log.error("Failed to upload profile image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to upload profile image: " + e.getMessage(),
                            null
                    ));
        }
    }

    /**
     * Delete profile image
     * @param authentication Current authenticated user
     * @return Success response
     */
    @DeleteMapping("/image")
    public ResponseEntity<ApiResponse<Void>> deleteProfileImage() {
        try {
            // Resolve the user from the SecurityContext (populated by HeaderAuthenticationFilter),
            // like the other methods here. A `UserPrincipal` method parameter cannot be used because
            // UserPrincipal implements java.security.Principal, so Spring's built-in
            // PrincipalMethodArgumentResolver intercepts it before our custom resolver and throws
            // "Current user principal is not of type [...]".
            Long userId = userUtil.getCurrentUserId();

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.of(
                                HttpStatus.UNAUTHORIZED.value(),
                                "User not authenticated",
                                null
                        ));
            }

            // Get current image reference
            String currentImage = userService.getUserProfileImage(userId);
            
            if (currentImage != null && !currentImage.isEmpty()) {
                // Delete from MinIO
                minioService.deleteProfileImage(currentImage);
                
                // Update user profile
                userService.updateProfileImage(userId, null);
            }
            
            return ResponseEntity.ok(
                    ApiResponse.of(
                            HttpStatus.OK.value(),
                            "Profile image deleted successfully",
                            null
                    )
            );
        } catch (Exception e) {
            log.error("Failed to delete profile image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to delete profile image: " + e.getMessage(),
                            null
                    ));
        }
    }

    /**
     * Get presigned URL for current profile image
     * @return Presigned URL for the authenticated user's profile image
     */
    @GetMapping("/image/url")
    public ResponseEntity<ApiResponse<Map<String, String>>> getProfileImageUrl() {
        try {
            Long userId = userUtil.getCurrentUserId();

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.of(
                                HttpStatus.UNAUTHORIZED.value(),
                                "User not authenticated",
                                null
                        ));
            }

            String objectName = userService.getUserProfileImage(userId);

            if (objectName == null || objectName.isEmpty()) {
                return ResponseEntity.ok(
                        ApiResponse.of(
                                HttpStatus.OK.value(),
                                "No profile image found",
                                Map.of("objectName", "", "imageUrl", "")
                        )
                );
            }

            String imageUrl = minioUtil.getPresignedUrl(objectName);

            return ResponseEntity.ok(
                    ApiResponse.of(
                            HttpStatus.OK.value(),
                            "Profile image URL retrieved successfully",
                            Map.of(
                                    "objectName", objectName,
                                    "imageUrl", imageUrl != null ? imageUrl : ""
                            )
                    )
            );
        } catch (Exception e) {
            log.error("Failed to get profile image URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to get profile image URL: " + e.getMessage(),
                            null
                    ));
        }
    }
}
