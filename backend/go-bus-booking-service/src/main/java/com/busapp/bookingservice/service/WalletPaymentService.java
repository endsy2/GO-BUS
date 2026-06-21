package com.busapp.bookingservice.service;

import com.busapp.bookingservice.dto.request.WalletPaymentBookingRequest;
import com.busapp.bookingservice.dto.response.BookingResponse;

public interface WalletPaymentService {
    BookingResponse walletBookingPayment(Long bookingId, String walletSessionToken, WalletPaymentBookingRequest request);
}
