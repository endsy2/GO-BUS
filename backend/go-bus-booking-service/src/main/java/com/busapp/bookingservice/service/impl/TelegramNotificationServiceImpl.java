package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.client.UserClient;
import com.busapp.bookingservice.config.TelegramConfig;
import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.dto.response.PaymentResponse;
import com.busapp.bookingservice.service.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramNotificationServiceImpl extends TelegramLongPollingBot implements TelegramNotificationService {

    private final TelegramConfig telegramConfig;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final UserClient userClient;

    @Override
    public String getBotUsername() {
        return "BusBookingAlertBot";
    }

    @Override
    public String getBotToken() {
        return telegramConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Handle incoming messages if needed
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            
            if ("/start".equals(messageText)) {
                sendTextMessage(chatId.toString(), 
                    "🚌 Bus Booking Alert Bot\n\n" +
                    "Your Chat ID: " + chatId + "\n\n" +
                    "Add this Chat ID to your configuration to receive alerts.");
            }
        }
    }

    @Async
    @Override
    public void sendBookingCreatedAlert(BookingResponse booking) {
        if (!telegramConfig.isEnabled()) return;

        String message = String.format(
            "🎫 NEW BOOKING CREATED\n\n" +
            "Booking ID: #%d\n" +
            "UserName: %s\n" +
            "Schedule ID: %d\n" +
            "Total Amount: $%.2f\n" +
            "Status: %s\n" +
            "Payment: %s\n" +
            "Created: %s",
            booking.getId(),
            booking.getFullName(),
            booking.getScheduleId(),
            booking.getTotalAmount(),
            booking.getBookingStatus(),
            booking.getPaymentStatus(),
            booking.getCreatedAt().format(FORMATTER)
        );

        sendTextMessage(telegramConfig.getChatId(), message);
    }

    @Async
    @Override
    public void sendPaymentCompletedAlert(BookingResponse booking, PaymentResponse payment) {
        if (!telegramConfig.isEnabled()) return;

        String message = String.format(
            "💰 PAYMENT COMPLETED\n\n" +
            "Booking ID: #%d\n" +
            "Payment ID: #%d\n" +
            "Amount: $%.2f\n" +
            "Method: %s\n" +
            "Status: %s\n" +
            "User ID: %d\n" +
            "Paid At: %s",
            booking.getId(),
            payment.getId(),
            payment.getAmount(),
            payment.getMethod(),
            payment.getStatus(),
                booking.getFullName(),
            payment.getPaidAt().format(FORMATTER)
        );

        sendTextMessage(telegramConfig.getChatId(), message);
    }

    @Async
    @Override
    public void sendBookingCancelledAlert(BookingResponse booking, String reason) {
        if (!telegramConfig.isEnabled()) return;

        String message = String.format(
            "❌ BOOKING CANCELLED\n\n" +
            "Booking ID: #%d\n" +
            "User ID: %s\n" +
            "Amount: $%.2f\n" +
            "Reason: %s\n" +
            "Cancelled At: %s",
            booking.getId(),
            booking.getFullName(),
            booking.getTotalAmount(),
            reason != null ? reason : "User requested",
            java.time.LocalDateTime.now().format(FORMATTER)
        );

        sendTextMessage(telegramConfig.getChatId(), message);
    }

    @Async
    @Override
    public void sendRefundProcessedAlert(Long bookingId, Double amount, String reason) {
        if (!telegramConfig.isEnabled()) return;

        String message = String.format(
            "💸 REFUND PROCESSED\n\n" +
            "Booking ID: #%d\n" +
            "Refund Amount: $%.2f\n" +
            "Reason: %s\n" +
            "Processed At: %s",
            bookingId,
            amount,
            reason != null ? reason : "N/A",
            java.time.LocalDateTime.now().format(FORMATTER)
        );

        sendTextMessage(telegramConfig.getChatId(), message);
    }

    @Async
    @Override
    public void sendAdminActionAlert(String action, BookingResponse booking, String adminInfo) {
        if (!telegramConfig.isEnabled()) return;

        String message = String.format(
            "⚡ ADMIN ACTION: %s\n\n" +
            "Booking ID: #%d\n" +
            "User ID: %s\n" +
            "Amount: $%.2f\n" +
            "New Status: %s\n" +
            "Admin: %s\n" +
            "Time: %s",
            action.toUpperCase(),
            booking.getId(),
                booking.getFullName(),
            booking.getTotalAmount(),
            booking.getBookingStatus(),
            adminInfo != null ? adminInfo : "System",
            java.time.LocalDateTime.now().format(FORMATTER)
        );

        sendTextMessage(telegramConfig.getChatId(), message);
    }

    @Async
    @Override
    public void sendCustomAlert(String message) {
        if (!telegramConfig.isEnabled()) return;
        sendTextMessage(telegramConfig.getChatId(), "🔔 " + message);
    }

    private void sendTextMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.enableMarkdown(false);

        try {
            execute(message);
            log.info("Telegram notification sent successfully to chat: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram notification: {}", e.getMessage(), e);
        }
    }
}
