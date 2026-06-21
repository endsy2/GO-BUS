package com.busapp.userservice.exception;

public class BakongApiException extends BakongPaymentException{
    private final Integer responseCode;

    public BakongApiException(String message) {
        super(message);
        this.responseCode = null;
    }

    public BakongApiException(String message, Integer responseCode) {
        super(message);
        this.responseCode = responseCode;
    }

    public BakongApiException(String message, Throwable cause) {
        super(message, cause);
        this.responseCode = null;
    }

    public Integer getResponseCode() {
        return responseCode;
    }
}
