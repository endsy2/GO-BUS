package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.response.BookingResponse;
import com.busapp.bookingservice.dto.response.PaymentResponse;

public interface NotificationPublisher {
    void publishBookingCreated(BookingResponse booking);
    void publishBookingCancelled(BookingResponse booking, String reason);
    void publishPaymentCompleted(BookingResponse booking, PaymentResponse payment);
    void publishRefundProcessed(Long bookingId, Double amount, String reason);
    void publishAdminAction(String action, BookingResponse booking, String adminInfo);
    void publishCustomAlert(String message);
}
