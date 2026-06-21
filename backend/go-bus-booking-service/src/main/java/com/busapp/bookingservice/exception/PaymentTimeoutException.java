package com.busapp.bookingservice.exception;

/**
 * Exception thrown when payment transaction check times out
 */
public class PaymentTimeoutException extends BakongPaymentException {
    public PaymentTimeoutException(String message) {
        super(message);
    }
    
    public PaymentTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
