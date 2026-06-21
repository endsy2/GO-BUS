package com.busapp.bookingservice.exception;

/**
 * Base exception for all Bakong payment-related errors
 */
public class BakongPaymentException extends RuntimeException {
    public BakongPaymentException(String message) {
        super(message);
    }
    
    public BakongPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
