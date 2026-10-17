package com.busapp.bookingservice.service.handler.impl;

import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.service.handler.PaymentHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KHQRHandlerImpl implements PaymentHandler {

    @Override
    public PaymentMethodType getType() {
        return PaymentMethodType.BAKONG;
    }
}
