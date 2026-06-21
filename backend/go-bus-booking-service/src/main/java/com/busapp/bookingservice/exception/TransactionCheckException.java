package com.busapp.bookingservice.exception;

/**
 * Exception thrown when transaction status check fails
 */
public class TransactionCheckException extends BakongPaymentException {
    public TransactionCheckException(String message) {
        super(message);
    }
    
    public TransactionCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
