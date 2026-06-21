package com.busapp.userservice.dto.response;

import com.busapp.userservice.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long             id;
    private Long             userId;
    private NotificationType type;
    private String           message;
    private Boolean          isRead;
    private LocalDateTime    createdAt;
}
