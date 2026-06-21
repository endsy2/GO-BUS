package com.busapp.bookingservice.exception;

/**
 * Exception thrown when payment is in an invalid state for the requested operation
 */
public class InvalidPaymentStateException extends BakongPaymentException {
    public InvalidPaymentStateException(String message) {
        super(message);
    }
    
    public InvalidPaymentStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
