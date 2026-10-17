package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.dto.request.BakongRequest;
import com.busapp.bookingservice.dto.request.CheckTransactionRequest;
import com.busapp.bookingservice.dto.response.BakongResponse;
import com.busapp.bookingservice.model.Payment;
import com.busapp.bookingservice.model.enums.PaymentStatus;

public interface BakongService {
    BakongResponse generateKhqr(BakongRequest bakongRequest);
    BakongResponse checkingTransaction(Long bookingId, CheckTransactionRequest checkTransactionRequest);
    void markSuccess (Long bookingId);
    void markFailure(PaymentStatus status, Long bookingId, String reason);
    Payment findByBookingId(Long bookingId);

}
