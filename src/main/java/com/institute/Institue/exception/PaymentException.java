package com.institute.Institue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class PaymentException extends RuntimeException {

    private final String code;

    public PaymentException(String message) {
        super(message);
        this.code = "PAYMENT_ERROR";
    }

    public PaymentException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() { return code; }
}
