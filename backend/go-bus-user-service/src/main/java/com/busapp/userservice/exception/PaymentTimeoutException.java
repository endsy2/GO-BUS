package com.busapp.userservice.exception;

public class PaymentTimeoutException extends BakongPaymentException{
    public PaymentTimeoutException(String message) {
        super(message);
    }

    public PaymentTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
