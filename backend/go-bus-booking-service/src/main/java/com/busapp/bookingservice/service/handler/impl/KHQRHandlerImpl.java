package com.busapp.bookingservice.service.handler.impl;

import com.busapp.bookingservice.model.Booking;
import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.service.BakongTokenService;
import com.busapp.bookingservice.service.handler.PaymentHandler;
import com.busapp.bookingservice.service.impl.BakongService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KHQRHandlerImpl implements PaymentHandler {

    @Override
    public void handle(Booking booking) {

    }

    @Override
    public PaymentMethodType getType() {
        return PaymentMethodType.BAKONG;
    }
}
