package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.config.RabbitMQConfig;
import com.busapp.bookingservice.dto.event.NotificationEvent;
import com.busapp.bookingservice.dto.event.NotificationPayload;
import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.service.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPublisherImpl implements NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishBookingCreated(BookingResponse booking) {
        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(NotificationEvent.NotificationType.BOOKING_CREATED)
                .message("New booking created")
                .payload(NotificationPayload.builder()
                        .booking(booking)
                        .build())
                .timestamp(LocalDateTime.now())
                .retryCount(0)
                .build();

        publishEvent(RabbitMQConfig.BOOKING_CREATED_KEY, event);
    }

    @Override
    public void publishBookingCancelled(BookingResponse booking, String reason) {
        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(NotificationEvent.NotificationType.BOOKING_CANCELLED)
                .message("Booking cancelled")
                .payload(NotificationPayload.builder()
                        .booking(booking)
                        .reason(reason)
                        .build())
                .timestamp(LocalDateTime.now())
                .retryCount(0)
                .build();

        publishEvent(RabbitMQConfig.BOOKING_CANCELLED_KEY, event);
    }

    @Override
    public void publishCustomAlert(String message) {
        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .type(NotificationEvent.NotificationType.CUSTOM_ALERT)
                .message(message)
                .payload(NotificationPayload.builder().build())
                .timestamp(LocalDateTime.now())
                .retryCount(0)
                .build();

        publishEvent(RabbitMQConfig.CUSTOM_ALERT_KEY, event);
    }

    private void publishEvent(String routingKey, NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    routingKey,
                    event
            );
            log.info("Published notification event: {} with routing key: {}", 
                    event.getType(), routingKey);
        } catch (Exception e) {
            log.error("Failed to publish notification event: {}", event.getType(), e);
            // Optionally: Store in database for retry or send alert
        }
    }
}
