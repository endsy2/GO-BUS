package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.event.DashboardUpdateEvent;
import com.busapp.bookingservice.dto.event.PaymentStatusEvent;
import com.busapp.bookingservice.dto.response.BookingResponse;

public interface WebSocketService {
    void broadcastPaymentStatus(PaymentStatusEvent event);
    void broadcastPaymentStatusToUser(Long userId, PaymentStatusEvent event);
    void broadcastDashboardUpdate(DashboardUpdateEvent event);

    /**
     * Broadcast a booking lifecycle change to the admin bookings list
     * (topic /topic/admin/bookings). {@code action} is one of
     * CREATED, PAYMENT_UPDATED, CANCELLED, UPDATED.
     */
    void broadcastBookingUpdate(String action, BookingResponse booking);
}
