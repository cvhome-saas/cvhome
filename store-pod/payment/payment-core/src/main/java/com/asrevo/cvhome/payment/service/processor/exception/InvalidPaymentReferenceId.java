package com.asrevo.cvhome.payment.service.processor.exception;

public class InvalidPaymentReferenceId extends RuntimeException {

    public InvalidPaymentReferenceId(String message) {
        super(message);
    }
}
