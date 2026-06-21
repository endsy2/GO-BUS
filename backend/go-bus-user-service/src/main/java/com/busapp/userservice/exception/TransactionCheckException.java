package com.busapp.userservice.exception;

public class TransactionCheckException extends BakongPaymentException{
    public TransactionCheckException(String message) {
        super(message);
    }

    public TransactionCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}