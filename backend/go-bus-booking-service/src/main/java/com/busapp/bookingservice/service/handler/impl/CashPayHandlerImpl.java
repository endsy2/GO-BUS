package com.busapp.bookingservice.service.handler.impl;

import com.busapp.bookingservice.model.Booking;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.service.handler.PaymentHandler;
import org.springframework.stereotype.Component;

@Component
public class CashPayHandlerImpl implements PaymentHandler {
    @Override
    public void handle(Booking booking) {

    }

    @Override
    public PaymentMethodType getType() {
        return PaymentMethodType.CASH;
    }
}
