package com.busapp.bookingservice.service.handler;

import com.busapp.bookingservice.model.Booking;
import com.busapp.bookingservice.model.enums.PaymentMethodType;

public interface PaymentHandler {
    void handle(Booking booking);
    PaymentMethodType getType();
}
