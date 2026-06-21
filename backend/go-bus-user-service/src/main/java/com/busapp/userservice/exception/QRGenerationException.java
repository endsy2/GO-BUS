package com.busapp.userservice.exception;

public class QRGenerationException extends BakongPaymentException {
    public QRGenerationException(String message) {
        super(message);
    }

    public QRGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
