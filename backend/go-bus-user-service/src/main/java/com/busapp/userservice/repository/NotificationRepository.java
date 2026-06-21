package com.busapp.userservice.repository;

import com.busapp.userservice.model.Notification;
import com.busapp.userservice.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead);
    List<Notification> findByUserIdAndType(Long userId, NotificationType type);
    long countByUserIdAndIsRead(Long userId, Boolean isRead);
}
