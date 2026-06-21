package com.busapp.bookingservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private NotificationType type;
    private String message;
    private NotificationPayload payload;
    private LocalDateTime timestamp;
    private Integer retryCount;
    
    public enum NotificationType {
        BOOKING_CREATED,
        BOOKING_CANCELLED,
        PAYMENT_COMPLETED,
        REFUND_PROCESSED,
        ADMIN_ACTION,
        CUSTOM_ALERT
    }
}
