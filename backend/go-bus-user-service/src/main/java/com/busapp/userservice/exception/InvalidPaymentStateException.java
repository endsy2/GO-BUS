package com.busapp.userservice.exception;

public class InvalidPaymentStateException extends BakongPaymentException{
    public InvalidPaymentStateException(String message) {
        super(message);
    }

    public InvalidPaymentStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
