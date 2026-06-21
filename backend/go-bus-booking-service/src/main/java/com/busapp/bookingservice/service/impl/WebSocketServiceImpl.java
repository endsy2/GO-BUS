package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.dto.event.BookingUpdateEvent;
import com.busapp.bookingservice.dto.event.DashboardUpdateEvent;
import com.busapp.bookingservice.dto.event.PaymentStatusEvent;
import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.service.WebSocketService;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send a STOMP message, deferring it until AFTER the current DB transaction
     * commits when one is active. Broadcasts are nearly always triggered from
     * inside an @Transactional service method; sending mid-transaction lets a
     * client receive the event and refetch BEFORE the write is committed, so it
     * reads stale data. Deferring to afterCommit guarantees subscribers only
     * react once the change is durable. With no active transaction (e.g. the
     * Bakong polling thread), it sends immediately.
     */
    private void sendAfterCommit(String destination, Object payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend(destination, payload);
                }
            });
        } else {
            messagingTemplate.convertAndSend(destination, payload);
        }
    }

    @Override
    public void broadcastPaymentStatus(PaymentStatusEvent event) {
        try {
        messagingTemplate.convertAndSend("/topic/payment/" + event.getPaymentId(), event);
        log.info("Broadcasted payment status event: type={}, paymentId={}, status={}",
                event.getType(), event.getPaymentId(), event.getStatus());
    } catch (Exception e) {
        log.error("Failed to broadcast payment status event", e);
    }
}

    @Override
    public void broadcastPaymentStatusToUser(Long userId, PaymentStatusEvent event) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(), 
                    "/queue/payment", 
                    event
            );
            log.info("Sent payment status to user: userId={}, type={}, status={}", 
                    userId, event.getType(), event.getStatus());
        } catch (Exception e) {
            log.error("Failed to send payment status to user", e);
        }
    }

    @Override
    public void broadcastDashboardUpdate(DashboardUpdateEvent event) {
        try {
            sendAfterCommit("/topic/admin/dashboard", event);
            log.info("Broadcasted dashboard update: type={}", event.getType());
        } catch (Exception e) {
            log.error("Failed to broadcast dashboard update", e);
        }
    }

    @Override
    public void broadcastBookingUpdate(String action, BookingResponse booking) {
        if (booking == null) {
            log.warn("Skipping booking update broadcast — null booking (action={})", action);
            return;
        }
        try {
            BookingUpdateEvent event = BookingUpdateEvent.builder()
                    .action(action)
                    .bookingId(booking.getId())
                    .booking(booking)
                    .timestamp(LocalDateTime.now())
                    .build();
            sendAfterCommit("/topic/admin/bookings", event);
            log.info("Broadcasted booking update: action={}, bookingId={}", action, booking.getId());

            // Any booking lifecycle change also makes the dashboard counters/charts
            // stale, so emit a trigger on the dashboard topic. The admin dashboard
            // treats it purely as a "refresh now" signal and re-pulls its data.
            DashboardUpdateEvent dashboardTrigger = DashboardUpdateEvent.builder()
                    .type("BOOKING_" + action)
                    .bookingId(booking.getId())
                    .amount(booking.getTotalAmount())
                    .timestamp(LocalDateTime.now())
                    .build();
            sendAfterCommit("/topic/admin/dashboard", dashboardTrigger);
        } catch (Exception e) {
            log.error("Failed to broadcast booking update", e);
        }
    }
}
