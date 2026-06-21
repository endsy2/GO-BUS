package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.dto.response.PaymentResponse;

public interface TelegramNotificationService {
    void sendBookingCreatedAlert(BookingResponse booking);
    void sendPaymentCompletedAlert(BookingResponse booking, PaymentResponse payment);
    void sendBookingCancelledAlert(BookingResponse booking, String reason);
    void sendRefundProcessedAlert(Long bookingId, Double amount, String reason);
    void sendAdminActionAlert(String action, BookingResponse booking, String adminInfo);
    void sendCustomAlert(String message);
}
