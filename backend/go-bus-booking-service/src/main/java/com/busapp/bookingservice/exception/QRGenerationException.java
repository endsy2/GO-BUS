package com.busapp.bookingservice.exception;

/**
 * Exception thrown when KHQR generation fails
 */
public class QRGenerationException extends BakongPaymentException {
    public QRGenerationException(String message) {
        super(message);
    }
    
    public QRGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
