package com.asrevo.cvhome.payment.service.processor.exception;

public class FailedPaymentInitiate extends RuntimeException {

    public FailedPaymentInitiate(String message, Exception e) {
        super(message, e);
    }
}
