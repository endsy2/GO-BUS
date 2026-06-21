package com.busapp.userservice.service.impl;

import com.busapp.userservice.dto.response.NotificationResponse;
import com.busapp.userservice.exception.ResourceNotFoundException;
import com.busapp.userservice.dto.mapper.NotificationMapper;
import com.busapp.userservice.model.Notification;
import com.busapp.userservice.model.enums.NotificationType;
import com.busapp.userservice.repository.NotificationRepository;
import com.busapp.userservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper     notificationMapper;

    @Override
    public List<NotificationResponse> getByUserId(Long userId) {
        return notificationRepository.findByUserId(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getUnreadByUserId(Long userId) {
        return notificationRepository.findByUserIdAndIsRead(userId, false)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getByUserIdAndType(Long userId, NotificationType type) {
        return notificationRepository.findByUserIdAndType(userId, type)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        log.debug("NOTIFICATION_MARK_READ", kv("notificationId", notificationId));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        notification.setIsRead(true);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        log.debug("NOTIFICATION_DELETE", kv("notificationId", notificationId));
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Notification not found: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
        log.debug("NOTIFICATION_DELETED", kv("notificationId", notificationId));
    }
}
