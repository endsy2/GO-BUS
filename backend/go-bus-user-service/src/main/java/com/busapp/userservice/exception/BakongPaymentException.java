package com.busapp.userservice.exception;

public class BakongPaymentException extends RuntimeException {
    public BakongPaymentException(String message) {
        super(message);
    }

    public BakongPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
