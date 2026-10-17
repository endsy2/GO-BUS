package com.busapp.bookingservice.service.handler;

import com.busapp.bookingservice.model.enums.PaymentMethodType;

public interface PaymentHandler {
    PaymentMethodType getType();
}
