package com.busapp.bookingservice.listener;

import com.busapp.bookingservice.config.RabbitMQConfig;
import com.busapp.bookingservice.dto.event.NotificationEvent;
import com.busapp.bookingservice.dto.event.NotificationPayload;
import com.busapp.bookingservice.service.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final TelegramNotificationService telegramService;

    /**
     * Single listener for all notification types
     * Processes messages from the unified notification queue
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotification(NotificationEvent event) {
        log.info("Received notification event: {} with ID: {}", 
                event.getType(), event.getEventId());

        try {
            NotificationPayload payload = event.getPayload();

            switch (event.getType()) {
                case BOOKING_CREATED -> {
                    if (payload.getBooking() != null) {
                        telegramService.sendBookingCreatedAlert(payload.getBooking());
                    }
                }
                case BOOKING_CANCELLED -> {
                    if (payload.getBooking() != null) {
                        telegramService.sendBookingCancelledAlert(
                                payload.getBooking(), 
                                payload.getReason()
                        );
                    }
                }
                case PAYMENT_COMPLETED -> {
                    if (payload.getBooking() != null && payload.getPayment() != null) {
                        telegramService.sendPaymentCompletedAlert(
                                payload.getBooking(), 
                                payload.getPayment()
                        );
                    }
                }
                case REFUND_PROCESSED -> {
                    if (payload.getBookingId() != null && payload.getRefundAmount() != null) {
                        telegramService.sendRefundProcessedAlert(
                                payload.getBookingId(),
                                payload.getRefundAmount(),
                                payload.getReason()
                        );
                    }
                }
                case ADMIN_ACTION -> {
                    if (payload.getBooking() != null && payload.getAction() != null) {
                        telegramService.sendAdminActionAlert(
                                payload.getAction(),
                                payload.getBooking(),
                                payload.getAdminInfo()
                        );
                    }
                }
                case CUSTOM_ALERT -> {
                    if (event.getMessage() != null) {
                        telegramService.sendCustomAlert(event.getMessage());
                    }
                }
                default -> log.warn("Unknown notification type: {}", event.getType());
            }

            log.info("Successfully processed notification event: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to process notification event: {}", event.getEventId(), e);
            // Exception will be caught by RabbitMQ and message will be sent to DLQ
            throw new RuntimeException("Failed to process notification", e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.DLQ_QUEUE)
    public void handleDeadLetterQueue(NotificationEvent event) {
        log.error("Message moved to DLQ - Event ID: {}, Type: {}, Retry Count: {}", 
                event.getEventId(), 
                event.getType(), 
                event.getRetryCount());
        
        // TODO: Implement DLQ handling logic
        // - Store in database for manual review
        // - Send alert to admin
        // - Implement retry mechanism with exponential backoff
    }
}
