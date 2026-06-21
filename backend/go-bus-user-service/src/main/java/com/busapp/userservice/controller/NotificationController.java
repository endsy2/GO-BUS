package com.busapp.userservice.controller;

import com.busapp.userservice.dto.response.ApiResponse;
import com.busapp.userservice.dto.response.NotificationResponse;
import com.busapp.userservice.model.enums.NotificationType;
import com.busapp.userservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.of(
                "Notifications retrieved successfully",
                notificationService.getByUserId(userId)));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.of(
                "Unread notifications retrieved successfully",
                notificationService.getUnreadByUserId(userId)));
    }

    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<ApiResponse<Long>> countUnreadByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.of(
                "Unread count retrieved successfully",
                notificationService.countUnreadByUserId(userId)));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getByUserAndType(
            @PathVariable Long userId,
            @PathVariable NotificationType type) {
        return ResponseEntity.ok(ApiResponse.of(
                "Notifications retrieved successfully",
                notificationService.getByUserIdAndType(userId, type)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(
                "Notification marked as read",
                notificationService.markAsRead(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Notification deleted successfully", null));
    }
}
