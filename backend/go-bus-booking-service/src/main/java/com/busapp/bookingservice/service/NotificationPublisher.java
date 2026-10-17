package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.response.BookingResponse;

public interface NotificationPublisher {
    void publishBookingCreated(BookingResponse booking);
    void publishBookingCancelled(BookingResponse booking, String reason);
    void publishCustomAlert(String message);
}
