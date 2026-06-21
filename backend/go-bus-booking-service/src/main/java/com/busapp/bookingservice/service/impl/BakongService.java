package com.busapp.bookingservice.service.impl;

import com.busapp.bookingservice.dto.request.BakongRequest;
import com.busapp.bookingservice.dto.request.CheckTransactionRequest;
import com.busapp.bookingservice.dto.request.PaymentRequest;
import com.busapp.bookingservice.dto.response.BakongResponse;
import com.busapp.bookingservice.dto.response.KhqrResponse;
import com.busapp.bookingservice.model.Payment;
import com.busapp.bookingservice.model.enums.Currency;
import com.busapp.bookingservice.model.enums.PaymentStatus;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public interface BakongService {
    BakongResponse generateKhqr(BakongRequest bakongRequest);
    BakongResponse checkingTransaction(Long bookingId, CheckTransactionRequest checkTransactionRequest);
    void paymentInfo();
    void createPending(String md5, Currency currency, Double amount, Long bookingId);
    void markSuccess (Long bookingId);
    void markFailure(PaymentStatus status, Long bookingId, String reason);
    Payment findByBookingId(Long bookingId);

}
