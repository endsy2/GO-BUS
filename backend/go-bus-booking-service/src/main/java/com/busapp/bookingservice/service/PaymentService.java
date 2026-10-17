package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.response.PaymentResponse;
import com.busapp.bookingservice.model.enums.PaymentMethodType;

public interface PaymentService {
    PaymentResponse getPaymentsByBooking(Long bookingId);
    PaymentResponse getPaymentById(Long id, PaymentMethodType paymentMethodId);
}
