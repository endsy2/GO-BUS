package com.busapp.bookingservice.service.handler.impl;

import com.busapp.bookingservice.model.enums.PaymentMethodType;
import com.busapp.bookingservice.service.handler.PaymentHandler;
import org.springframework.stereotype.Component;

@Component
public class WalletHandlerImpl implements PaymentHandler {

    @Override
    public PaymentMethodType getType() {
        return PaymentMethodType.WALLET;
    }
}
