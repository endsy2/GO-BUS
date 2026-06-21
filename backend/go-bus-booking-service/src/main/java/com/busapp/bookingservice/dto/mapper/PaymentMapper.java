package com.busapp.bookingservice.dto.mapper;

import com.busapp.bookingservice.dto.request.PaymentRequest;
import com.busapp.bookingservice.dto.response.PaymentResponse;
import com.busapp.bookingservice.model.Payment;
import com.busapp.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentMapper {
    private final BookingRepository  bookingRepository;
    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking() != null ? payment.getBooking().getId() : null)
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .walletTransactionId(payment.getWalletTransactionId())
                .build();
    }
    public Payment toEntity(PaymentRequest paymentRequest) {
        return Payment.builder()
                .transactionId(paymentRequest.getTransactionId())
                .booking(bookingRepository.findById(paymentRequest.getBookingId()).orElse(null))
                .walletTransactionId(paymentRequest.getWalletTransactionId())
                .method(paymentRequest.getPaymentMethodType())
                .amount(paymentRequest.getAmount())
                .status(paymentRequest.getPaymentStatus())
                .paidAt(LocalDateTime.now())
                .build();
    }
}