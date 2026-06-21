package com.busapp.userservice.exception;

public class WalletNotAuthenticatedException extends RuntimeException {
    public WalletNotAuthenticatedException(String message) {
        super(message);
    }

    public WalletNotAuthenticatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
