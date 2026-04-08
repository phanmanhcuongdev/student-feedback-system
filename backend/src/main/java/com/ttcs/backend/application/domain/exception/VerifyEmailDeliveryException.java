package com.ttcs.backend.application.domain.exception;

public class VerifyEmailDeliveryException extends RuntimeException {

    public VerifyEmailDeliveryException(String message) {
        super(message);
    }

    public VerifyEmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
