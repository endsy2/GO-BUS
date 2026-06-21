package com.busapp.userservice.service;

import com.busapp.userservice.dto.response.NotificationResponse;
import com.busapp.userservice.model.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getByUserId(Long userId);

    List<NotificationResponse> getUnreadByUserId(Long userId);

    List<NotificationResponse> getByUserIdAndType(Long userId, NotificationType type);

    long countUnreadByUserId(Long userId);

    NotificationResponse markAsRead(Long notificationId);

    void deleteNotification(Long notificationId);
}
