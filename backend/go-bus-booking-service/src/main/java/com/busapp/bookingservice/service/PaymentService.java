package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.request.PaymentRequest;
import com.busapp.bookingservice.dto.response.PaymentResponse;
import com.busapp.bookingservice.model.enums.PaymentMethodType;

import java.util.List;

public interface PaymentService {
    PaymentResponse getPaymentsByBooking(Long bookingId);
    PaymentResponse getPaymentById(Long id, PaymentMethodType paymentMethodId);
    void createPayment(Long id, PaymentRequest paymentRequest);
}
